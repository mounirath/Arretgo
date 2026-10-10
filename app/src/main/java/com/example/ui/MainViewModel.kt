package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FavoritePlace
import com.example.data.FavoritesRepository
import com.example.data.RecentTrip
import com.example.data.RecentTripsRepository
import com.example.model.AlarmTone
import com.example.model.AppLanguage
import com.example.model.FavoriteAlertEvent
import com.example.model.LocationPoint
import com.example.model.MapStyle
import com.example.model.TripState
import com.example.model.UserLocation
import com.example.service.LocationTracker
import com.example.service.SearchService
import com.example.service.SoundVibrationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val favoritesRepository = FavoritesRepository(db.favoriteDao())
    val favorites: StateFlow<List<FavoritePlace>> = favoritesRepository.allFavorites
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val recentTripsRepository = RecentTripsRepository(db.recentTripDao())
    val recentTrips: StateFlow<List<RecentTrip>> = recentTripsRepository.allRecentTrips
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val soundVibrationManager = SoundVibrationManager(application)
    val locationTracker = LocationTracker(application)
    private val searchService = SearchService()

    val userLocation: StateFlow<UserLocation> = locationTracker.userLocation

    // UI Configuration States
    private val _destination = MutableStateFlow<LocationPoint?>(
        LocationPoint(
            name = "Paris",
            address = "Paris, Île-de-France, France",
            latitude = 48.8566,
            longitude = 2.3522
        )
    )
    val destination: StateFlow<LocationPoint?> = _destination.asStateFlow()

    private val _alertRadius = MutableStateFlow(500) // 500 meters default
    val alertRadius: StateFlow<Int> = _alertRadius.asStateFlow()

    private val _alarmTone = MutableStateFlow(AlarmTone.SIREN)
    val alarmTone: StateFlow<AlarmTone> = _alarmTone.asStateFlow()

    private val _mapStyle = MutableStateFlow(MapStyle.GOOGLE_MAPS)
    val mapStyle: StateFlow<MapStyle> = _mapStyle.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.AR)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(true)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _isTestingTone = MutableStateFlow(false)
    val isTestingTone: StateFlow<Boolean> = _isTestingTone.asStateFlow()

    private val _isSimulationMode = MutableStateFlow(false)
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    // Trip State
    private val _tripState = MutableStateFlow(TripState())
    val tripState: StateFlow<TripState> = _tripState.asStateFlow()

    // Favorite Proximity Alert State
    private val _favoriteAlertEvent = MutableStateFlow<FavoriteAlertEvent?>(null)
    val favoriteAlertEvent: StateFlow<FavoriteAlertEvent?> = _favoriteAlertEvent.asStateFlow()

    private val _isFavoriteProximityAlertEnabled = MutableStateFlow(true)
    val isFavoriteProximityAlertEnabled: StateFlow<Boolean> = _isFavoriteProximityAlertEnabled.asStateFlow()

    private val acknowledgedFavoriteIds = mutableSetOf<Long>()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<LocationPoint>>(emptyList())
    val searchResults: StateFlow<List<LocationPoint>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Observe location updates and compute distance when trip is active
        viewModelScope.launch {
            userLocation.collect { userLoc ->
                val dest = _destination.value
                val trip = _tripState.value
                if (dest != null) {
                    val dist = LocationTracker.calculateDistanceMeters(
                        userLoc.latitude,
                        userLoc.longitude,
                        dest.latitude,
                        dest.longitude
                    )

                    val radius = _alertRadius.value
                    val isInside = dist <= radius

                    if (trip.isActive) {
                        _tripState.value = trip.copy(
                            currentDistanceMeters = dist,
                            isWithinAlertZone = isInside,
                            isAlarmRinging = if (isInside && !trip.isAlarmRinging) true else trip.isAlarmRinging
                        )

                        // Trigger Sound & Vibration if entering alert zone
                        if (isInside && !_tripState.value.isAlarmMuted) {
                            soundVibrationManager.startAlarmSound(_alarmTone.value)
                            if (_isVibrationEnabled.value) {
                                soundVibrationManager.startVibration()
                            }
                        }
                    }
                }

                // Automatic Favorite Proximity Detection
                if (_isFavoriteProximityAlertEnabled.value) {
                    checkFavoritesProximity(userLoc)
                }
            }
        }
    }

    private fun checkFavoritesProximity(userLoc: UserLocation) {
        val currentFavs = favorites.value
        if (currentFavs.isEmpty()) return

        var nearestApproachingFav: Pair<FavoritePlace, Float>? = null

        for (fav in currentFavs) {
            val dist = LocationTracker.calculateDistanceMeters(
                userLoc.latitude,
                userLoc.longitude,
                fav.latitude,
                fav.longitude
            )

            val threshold = fav.defaultRadiusMeters.toFloat()

            // If user moved far away from an acknowledged favorite (> 1.5x threshold), reset it
            if (dist > threshold * 1.5f) {
                acknowledgedFavoriteIds.remove(fav.id)
            }

            // Check if user is within the alert radius of this favorite
            if (dist <= threshold && !acknowledgedFavoriteIds.contains(fav.id)) {
                if (nearestApproachingFav == null || dist < nearestApproachingFav.second) {
                    nearestApproachingFav = Pair(fav, dist)
                }
            }
        }

        if (nearestApproachingFav != null) {
            val (fav, dist) = nearestApproachingFav
            val currentAlert = _favoriteAlertEvent.value

            if (currentAlert?.favorite?.id != fav.id) {
                _favoriteAlertEvent.value = FavoriteAlertEvent(
                    favorite = fav,
                    distanceMeters = dist,
                    isRinging = true,
                    isMuted = false
                )

                // Trigger Sound & Vibration for Favorite Arrival!
                soundVibrationManager.startAlarmSound(_alarmTone.value)
                if (_isVibrationEnabled.value) {
                    soundVibrationManager.startVibration()
                }
            } else {
                _favoriteAlertEvent.value = currentAlert.copy(distanceMeters = dist)
            }
        }
    }

    fun dismissFavoriteAlert() {
        soundVibrationManager.stopAlarmSound()
        soundVibrationManager.stopVibration()
        val current = _favoriteAlertEvent.value
        if (current != null) {
            acknowledgedFavoriteIds.add(current.favorite.id)
        }
        _favoriteAlertEvent.value = null
    }

    fun muteFavoriteAlert() {
        soundVibrationManager.stopAlarmSound()
        soundVibrationManager.stopVibration()
        _favoriteAlertEvent.value = _favoriteAlertEvent.value?.copy(isMuted = true, isRinging = false)
    }

    fun setFavoriteProximityAlertEnabled(enabled: Boolean) {
        _isFavoriteProximityAlertEnabled.value = enabled
        if (!enabled) {
            dismissFavoriteAlert()
        }
    }

    fun setDestination(point: LocationPoint) {
        _destination.value = point
        val userLoc = userLocation.value
        val initialDist = LocationTracker.calculateDistanceMeters(
            userLoc.latitude,
            userLoc.longitude,
            point.latitude,
            point.longitude
        )
        if (_tripState.value.isActive) {
            _tripState.value = _tripState.value.copy(
                destination = point,
                currentDistanceMeters = initialDist,
                initialDistanceMeters = initialDist
            )
        }
    }

    fun setMapClickedPoint(lat: Double, lng: Double) {
        val approxName = "Point sélectionné (%.4f, %.4f)".format(lat, lng)
        setDestination(LocationPoint(approxName, "Coordonnées GPS directes", lat, lng))
    }

    fun clearDestination() {
        stopTrip()
        _destination.value = null
    }

    fun setAlertRadius(meters: Int) {
        _alertRadius.value = meters.coerceIn(50, 10000)
        if (_tripState.value.isActive) {
            _tripState.value = _tripState.value.copy(alertRadiusMeters = _alertRadius.value)
        }
    }

    fun setAlarmTone(tone: AlarmTone) {
        _alarmTone.value = tone
    }

    fun setMapStyle(style: MapStyle) {
        _mapStyle.value = style
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _isVibrationEnabled.value = enabled
        if (!enabled) {
            soundVibrationManager.stopVibration()
        }
    }

    fun testToneToggle() {
        if (_isTestingTone.value) {
            soundVibrationManager.stopAlarmSound()
            _isTestingTone.value = false
        } else {
            _isTestingTone.value = true
            soundVibrationManager.testTone(_alarmTone.value) {
                _isTestingTone.value = false
            }
        }
    }

    fun testSpecificTone(tone: AlarmTone) {
        if (_isTestingTone.value) {
            soundVibrationManager.stopAlarmSound()
            _isTestingTone.value = false
        } else {
            _isTestingTone.value = true
            soundVibrationManager.testTone(tone) {
                _isTestingTone.value = false
            }
        }
    }

    fun startTrip() {
        val dest = _destination.value ?: return
        val userLoc = userLocation.value
        val initialDist = LocationTracker.calculateDistanceMeters(
            userLoc.latitude,
            userLoc.longitude,
            dest.latitude,
            dest.longitude
        )

        _tripState.value = TripState(
            isActive = true,
            destination = dest,
            alertRadiusMeters = _alertRadius.value,
            currentDistanceMeters = initialDist,
            initialDistanceMeters = initialDist,
            isWithinAlertZone = initialDist <= _alertRadius.value,
            isAlarmRinging = false,
            isAlarmMuted = false
        )

        if (_isSimulationMode.value) {
            locationTracker.startSimulation(dest, _alertRadius.value)
        } else {
            locationTracker.startRealLocationUpdates()
        }

        // Record trip into history
        viewModelScope.launch {
            recentTripsRepository.recordTrip(
                RecentTrip(
                    destinationName = dest.name,
                    destinationAddress = dest.address,
                    latitude = dest.latitude,
                    longitude = dest.longitude,
                    alertRadiusMeters = _alertRadius.value,
                    initialDistanceMeters = initialDist,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteRecentTrip(trip: RecentTrip) {
        viewModelScope.launch {
            recentTripsRepository.deleteTrip(trip)
        }
    }

    fun clearRecentTrips() {
        viewModelScope.launch {
            recentTripsRepository.clearHistory()
        }
    }

    fun stopTrip() {
        soundVibrationManager.stopAlarmSound()
        soundVibrationManager.stopVibration()
        locationTracker.stopSimulation()
        _tripState.value = TripState(isActive = false)
    }

    fun dismissAlarm() {
        soundVibrationManager.stopAlarmSound()
        soundVibrationManager.stopVibration()
        _tripState.value = _tripState.value.copy(
            isAlarmRinging = false,
            isAlarmMuted = true
        )
    }

    fun toggleMute() {
        val newMuted = !_tripState.value.isAlarmMuted
        _tripState.value = _tripState.value.copy(isAlarmMuted = newMuted)
        if (newMuted) {
            soundVibrationManager.stopAlarmSound()
        } else if (_tripState.value.isWithinAlertZone) {
            soundVibrationManager.startAlarmSound(_alarmTone.value)
        }
    }

    fun setSimulationMode(enabled: Boolean) {
        _isSimulationMode.value = enabled
        if (enabled && _tripState.value.isActive) {
            _destination.value?.let { dest ->
                locationTracker.startSimulation(dest, _alertRadius.value)
            }
        } else if (!enabled) {
            locationTracker.stopSimulation()
            locationTracker.startRealLocationUpdates()
        }
    }

    fun onSearchQueryChanged(q: String) {
        _searchQuery.value = q
        searchJob?.cancel()
        if (q.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        _isSearching.value = true
        searchJob = viewModelScope.launch {
            delay(350)
            val userLoc = userLocation.value
            val results = searchService.searchPlaces(
                query = q,
                userLatitude = userLoc.latitude,
                userLongitude = userLoc.longitude
            )
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun saveFavorite(name: String, tag: String) {
        val dest = _destination.value ?: return
        viewModelScope.launch {
            favoritesRepository.addFavorite(
                FavoritePlace(
                    name = name,
                    address = dest.address,
                    latitude = dest.latitude,
                    longitude = dest.longitude,
                    tag = tag,
                    defaultRadiusMeters = _alertRadius.value
                )
            )
        }
    }

    fun deleteFavorite(place: FavoritePlace) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(place)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundVibrationManager.stopAlarmSound()
        soundVibrationManager.stopVibration()
        locationTracker.stopLocationUpdates()
    }
}
