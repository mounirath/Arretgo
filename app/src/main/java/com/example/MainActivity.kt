package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppLanguage
import com.example.model.FavoriteAlertEvent
import com.example.model.LocationPoint
import com.example.model.MapStyle
import com.example.service.AppOpenAdManager
import com.example.service.LocationTracker
import com.example.service.TrackingForegroundService
import com.example.ui.MainViewModel
import com.example.ui.components.AddFavoriteDialog
import com.example.ui.components.AlarmOverlay
import com.example.ui.components.FavoriteProximityAlertOverlay
import com.example.ui.components.FavoritesDialog
import com.example.ui.components.GlassBackdropMesh
import com.example.ui.components.GoogleMapsBottomSheet
import com.example.ui.components.GoogleMapsTopBar
import com.example.ui.components.MapFloatingControls
import com.example.ui.components.NavigationMenuDialog
import com.example.ui.components.TripHistoryDialog
import com.example.ui.components.TripHud
import com.example.ui.map.ArrivaMapView
import com.example.ui.map.GoogleMapsComposeView
import com.example.ui.theme.ArrivaTheme
import com.google.android.gms.maps.MapsInitializer
import android.util.Log

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appOpenAdManager = AppOpenAdManager(applicationContext)
        appOpenAdManager.loadAd()

        // Initialize Google Maps SDK in Activity
        try {
            MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { renderer ->
                Log.i("MainActivity", "Google Maps SDK initialized: $renderer")
            }
        } catch (e: Exception) {
            try {
                MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LEGACY) { renderer ->
                    Log.i("MainActivity", "Google Maps SDK LEGACY initialized: $renderer")
                }
            } catch (ex: Exception) {
                Log.e("MainActivity", "Error initializing MapsInitializer", ex)
            }
        }

        setContent {
            val language by viewModel.language.collectAsStateWithLifecycle()
            val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                ArrivaTheme(darkTheme = true) {
                    ArrivaAppScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appOpenAdManager.showAdIfAvailable(this)
    }
}

@Composable
fun ArrivaAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    // Request Location Permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.locationTracker.startRealLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (fineCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.locationTracker.startRealLocationUpdates()
        } else {
            val perms = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(perms.toTypedArray())
        }
    }

    // States from ViewModel
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val alertRadius by viewModel.alertRadius.collectAsStateWithLifecycle()
    val alarmTone by viewModel.alarmTone.collectAsStateWithLifecycle()
    val mapStyle by viewModel.mapStyle.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsStateWithLifecycle()
    val isTestingTone by viewModel.isTestingTone.collectAsStateWithLifecycle()
    val isSimulationMode by viewModel.isSimulationMode.collectAsStateWithLifecycle()
    val tripState by viewModel.tripState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favoriteAlertEvent by viewModel.favoriteAlertEvent.collectAsStateWithLifecycle()
    val isFavoriteAlertsEnabled by viewModel.isFavoriteProximityAlertEnabled.collectAsStateWithLifecycle()
    val recentTrips by viewModel.recentTrips.collectAsStateWithLifecycle()

    var isFavoritesManagerOpen by remember { mutableStateOf(false) }
    var isTripHistoryOpen by remember { mutableStateOf(false) }
    var isAddFavoriteDialogOpen by remember { mutableStateOf(false) }
    var isNavigationMenuOpen by remember { mutableStateOf(false) }
    var isFullscreenMap by remember { mutableStateOf(false) }
    var isSheetExpanded by remember { mutableStateOf(true) }

    // Map control triggers
    var centerUserTrigger by remember { mutableStateOf(0L) }
    var centerDestTrigger by remember { mutableStateOf(0L) }
    var zoomInTrigger by remember { mutableStateOf(0L) }
    var zoomOutTrigger by remember { mutableStateOf(0L) }

    // Live distance calculation
    val currentDistance = remember(userLocation.latitude, userLocation.longitude, destination) {
        val dest = destination
        if (dest != null) {
            LocationTracker.calculateDistanceMeters(
                userLocation.latitude,
                userLocation.longitude,
                dest.latitude,
                dest.longitude
            )
        } else Float.MAX_VALUE
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Layer 1: Google Maps Fullscreen View (Unrestricted opaque canvas)
            val hasValidMapsKey = BuildConfig.MAPS_API_KEY.isNotBlank() &&
                !BuildConfig.MAPS_API_KEY.contains("YOUR_GOOGLE_MAPS_API_KEY")

            val onSelectFavoriteStop: (com.example.data.FavoritePlace) -> Unit = { fav ->
                viewModel.setDestination(
                    LocationPoint(
                        name = fav.name,
                        address = fav.address,
                        latitude = fav.latitude,
                        longitude = fav.longitude
                    )
                )
                viewModel.setAlertRadius(fav.defaultRadiusMeters)
                centerDestTrigger++
            }

            // Layer 1: Map View (OpenStreetMap via Leaflet or Native Google Maps SDK)
            if (mapStyle == MapStyle.OPENSTREETMAP) {
                ArrivaMapView(
                    userLocation = userLocation,
                    destination = destination,
                    alertRadiusMeters = alertRadius,
                    mapStyle = mapStyle,
                    favorites = favorites,
                    onSelectFavorite = onSelectFavoriteStop,
                    onMapClick = { lat, lng ->
                        viewModel.setMapClickedPoint(lat, lng)
                    },
                    centerUserTrigger = centerUserTrigger,
                    centerDestTrigger = centerDestTrigger,
                    zoomInTrigger = zoomInTrigger,
                    zoomOutTrigger = zoomOutTrigger
                )
            } else {
                GoogleMapsComposeView(
                    userLocation = userLocation,
                    destination = destination,
                    alertRadiusMeters = alertRadius,
                    mapStyle = mapStyle,
                    favorites = favorites,
                    onSelectFavorite = onSelectFavoriteStop,
                    onMapClick = { lat, lng ->
                        viewModel.setMapClickedPoint(lat, lng)
                    },
                    centerUserTrigger = centerUserTrigger,
                    centerDestTrigger = centerDestTrigger,
                    zoomInTrigger = zoomInTrigger,
                    zoomOutTrigger = zoomOutTrigger
                )
            }

            // Layer 2: Right-Hand Floating Controls (Center, Fullscreen, Layers, + , -)
            MapFloatingControls(
                onCenterLocation = { centerUserTrigger++ },
                isFullscreen = isFullscreenMap,
                onToggleFullscreen = { isFullscreenMap = !isFullscreenMap },
                currentMapStyle = mapStyle,
                onMapStyleChange = viewModel::setMapStyle,
                onZoomIn = { zoomInTrigger++ },
                onZoomOut = { zoomOutTrigger++ },
                currentLanguage = language,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(bottom = if (isFullscreenMap) 20.dp else 130.dp)
            )

            // Layer 3: Overlaid UI (Top Search & Bottom Sheets)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                // Top Header, Brand & Google Search Bar
                GoogleMapsTopBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    searchResults = searchResults,
                    isSearching = isSearching,
                    onSelectPlace = { place ->
                        viewModel.setDestination(place)
                        viewModel.onSearchQueryChanged("")
                        centerDestTrigger++
                    },
                    currentLanguage = language,
                    onLanguageChange = viewModel::setLanguage,
                    currentMapStyle = mapStyle,
                    onMapStyleChange = viewModel::setMapStyle,
                    userLocation = userLocation,
                    onOpenMenu = { isNavigationMenuOpen = true },
                    onOpenMapLayers = { isFullscreenMap = !isFullscreenMap }
                )

                // Trip Active HUD Bar
                TripHud(
                    tripState = tripState,
                    userLocation = userLocation,
                    currentLanguage = language,
                    onStopTrip = {
                        viewModel.stopTrip()
                        TrackingForegroundService.stop(context)
                    },
                    isDarkTerrain = mapStyle == MapStyle.SATELLITE || mapStyle == MapStyle.DARK
                )

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Configuration & Actions Sheet
                AnimatedVisibility(
                    visible = !isFullscreenMap,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    GoogleMapsBottomSheet(
                        destination = destination,
                        alertRadiusMeters = alertRadius,
                        onRadiusChange = viewModel::setAlertRadius,
                        userDistanceMeters = currentDistance,
                        isTripActive = tripState.isActive,
                        onStartTrip = {
                            viewModel.startTrip()
                            destination?.let { dest ->
                                TrackingForegroundService.start(
                                    context = context,
                                    destName = dest.name,
                                    lat = dest.latitude,
                                    lng = dest.longitude,
                                    radiusMeters = alertRadius
                                )
                            }
                        },
                        onStopTrip = {
                            viewModel.stopTrip()
                            TrackingForegroundService.stop(context)
                        },
                        onClearDestination = viewModel::clearDestination,
                        onFocusDestinationOnMap = { centerDestTrigger++ },
                        onSaveToFavorites = { isAddFavoriteDialogOpen = true },
                        favorites = favorites,
                        onSelectFavorite = { fav ->
                            viewModel.setDestination(
                                LocationPoint(
                                    name = fav.name,
                                    address = fav.address,
                                    latitude = fav.latitude,
                                    longitude = fav.longitude
                                )
                            )
                            viewModel.setAlertRadius(fav.defaultRadiusMeters)
                            centerDestTrigger++
                        },
                        onOpenFavoritesManager = { isFavoritesManagerOpen = true },
                        onOpenTripHistory = { isTripHistoryOpen = true },
                        isExpanded = isSheetExpanded,
                        onToggleExpand = { isSheetExpanded = !isSheetExpanded },
                        currentLanguage = language,
                        onTestToneToggle = viewModel::testToneToggle,
                        isSimulationMode = isSimulationMode,
                        onSimulationToggle = viewModel::setSimulationMode
                    )
                }
            }

            // Layer 4: High Priority Urgent Alarm Overlay (Triggered upon reaching geofence perimeter)
            AlarmOverlay(
                tripState = tripState,
                currentLanguage = language,
                onDismissAlarm = viewModel::dismissAlarm,
                onMuteToggle = viewModel::toggleMute
            )

            // Layer 4b: Favorite Proximity Alert Overlay (Sound / Vibration upon approaching a favorite zone)
            FavoriteProximityAlertOverlay(
                alertEvent = favoriteAlertEvent,
                currentLanguage = language,
                onDismissAlert = viewModel::dismissFavoriteAlert,
                onMuteAlert = viewModel::muteFavoriteAlert,
                onSetAsDestination = {
                    favoriteAlertEvent?.let { ev ->
                        viewModel.setDestination(
                            LocationPoint(
                                name = ev.favorite.name,
                                address = ev.favorite.address,
                                latitude = ev.favorite.latitude,
                                longitude = ev.favorite.longitude
                            )
                        )
                        viewModel.setAlertRadius(ev.favorite.defaultRadiusMeters)
                        viewModel.startTrip()
                        TrackingForegroundService.start(
                            context = context,
                            destName = ev.favorite.name,
                            lat = ev.favorite.latitude,
                            lng = ev.favorite.longitude,
                            radiusMeters = ev.favorite.defaultRadiusMeters
                        )
                        viewModel.dismissFavoriteAlert()
                        centerDestTrigger++
                    }
                }
            )

            // Navigation / Settings Menu Dialog (Hamburger icon matching Screenshot 2)
            NavigationMenuDialog(
                isOpen = isNavigationMenuOpen,
                onDismiss = { isNavigationMenuOpen = false },
                currentLanguage = language,
                onLanguageChange = viewModel::setLanguage,
                selectedTone = alarmTone,
                onToneChange = viewModel::setAlarmTone,
                isTestingTone = isTestingTone,
                onTestToneToggle = viewModel::testToneToggle,
                isVibrationEnabled = isVibrationEnabled,
                onVibrationToggle = viewModel::setVibrationEnabled,
                isSimulationMode = isSimulationMode,
                onSimulationToggle = viewModel::setSimulationMode,
                currentMapStyle = mapStyle,
                onToggleMapStyle = {
                    viewModel.setMapStyle(
                        if (mapStyle == MapStyle.OPENSTREETMAP) MapStyle.GOOGLE_MAPS else MapStyle.OPENSTREETMAP
                    )
                },
                userLocation = userLocation,
                isFavoriteAlertsEnabled = isFavoriteAlertsEnabled,
                onToggleFavoriteAlerts = viewModel::setFavoriteProximityAlertEnabled,
                onOpenFavoritesManager = {
                    isNavigationMenuOpen = false
                    isFavoritesManagerOpen = true
                },
                onOpenTripHistory = {
                    isNavigationMenuOpen = false
                    isTripHistoryOpen = true
                }
            )

            // Dialog: Recent Trip History Screen
            TripHistoryDialog(
                isOpen = isTripHistoryOpen,
                onDismiss = { isTripHistoryOpen = false },
                recentTrips = recentTrips,
                onRelaunchTrip = { trip ->
                    viewModel.setDestination(
                        LocationPoint(
                            name = trip.destinationName,
                            address = trip.destinationAddress,
                            latitude = trip.latitude,
                            longitude = trip.longitude
                        )
                    )
                    viewModel.setAlertRadius(trip.alertRadiusMeters)
                    viewModel.startTrip()
                    TrackingForegroundService.start(
                        context = context,
                        destName = trip.destinationName,
                        lat = trip.latitude,
                        lng = trip.longitude,
                        radiusMeters = trip.alertRadiusMeters
                    )
                    isTripHistoryOpen = false
                    centerDestTrigger++
                },
                onSaveToFavorites = { trip ->
                    viewModel.saveFavorite(trip.destinationName, "Gare")
                },
                onDeleteTrip = viewModel::deleteRecentTrip,
                onClearAllHistory = viewModel::clearRecentTrips,
                currentLanguage = language
            )

            // Dialog: Favorites List & Management
            FavoritesDialog(
                isOpen = isFavoritesManagerOpen,
                onDismiss = { isFavoritesManagerOpen = false },
                favorites = favorites,
                onSelectFavorite = { fav ->
                    viewModel.setDestination(
                        LocationPoint(
                            name = fav.name,
                            address = fav.address,
                            latitude = fav.latitude,
                            longitude = fav.longitude
                        )
                    )
                    viewModel.setAlertRadius(fav.defaultRadiusMeters)
                    isFavoritesManagerOpen = false
                    centerDestTrigger++
                },
                onDeleteFavorite = viewModel::deleteFavorite,
                currentLanguage = language
            )

            // Dialog: Add Destination to Favorites
            AddFavoriteDialog(
                isOpen = isAddFavoriteDialogOpen,
                onDismiss = { isAddFavoriteDialogOpen = false },
                initialName = destination?.name ?: "",
                initialAddress = destination?.address ?: "",
                onConfirm = { name, tag ->
                    viewModel.saveFavorite(name, tag)
                    isAddFavoriteDialogOpen = false
                },
                currentLanguage = language
            )
        }
    }
}
