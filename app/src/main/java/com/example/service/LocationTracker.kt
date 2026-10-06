package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.example.model.LocationPoint
import com.example.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LocationTracker(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _userLocation = MutableStateFlow(
        // Default initial point (near Paris Gare de Lyon or general transit stop)
        UserLocation(
            latitude = 48.8566,
            longitude = 2.3522,
            accuracyMeters = 15f,
            speedKmh = 0f,
            isGpsActive = false,
            isSimulated = false
        )
    )
    val userLocation: StateFlow<UserLocation> = _userLocation.asStateFlow()

    private var fusedCallback: LocationCallback? = null
    private var isTracking = false
    private var simulationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    @SuppressLint("MissingPermission")
    fun startRealLocationUpdates() {
        stopSimulation()
        if (isTracking) return
        isTracking = true

        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(2f)
                .build()

            fusedCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { loc ->
                        updateFromAndroidLocation(loc, isSimulated = false)
                    }
                }
            }

            fusedClient.requestLocationUpdates(
                locationRequest,
                fusedCallback!!,
                Looper.getMainLooper()
            ).addOnFailureListener {
                fallbackToLocationManager()
            }

            // Also request last known location immediately
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    updateFromAndroidLocation(loc, isSimulated = false)
                }
            }
        } catch (e: SecurityException) {
            fallbackToLocationManager()
        } catch (e: Exception) {
            fallbackToLocationManager()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackToLocationManager() {
        try {
            locationManager?.let { lm ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        updateFromAndroidLocation(loc, isSimulated = false)
                    }
                    override fun onProviderDisabled(provider: String) {}
                    override fun onProviderEnabled(provider: String) {}
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 2f, listener)
                }
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 5f, listener)
                }
            }
        } catch (e: Exception) {
            // Permissions not granted yet
        }
    }

    private fun updateFromAndroidLocation(loc: Location, isSimulated: Boolean) {
        val speedKmh = (loc.speed * 3.6f).coerceAtLeast(0f)
        _userLocation.value = UserLocation(
            latitude = loc.latitude,
            longitude = loc.longitude,
            accuracyMeters = loc.accuracy,
            speedKmh = speedKmh,
            isGpsActive = true,
            isSimulated = isSimulated
        )
    }

    fun stopLocationUpdates() {
        isTracking = false
        fusedCallback?.let {
            fusedClient.removeLocationUpdates(it)
            fusedCallback = null
        }
        stopSimulation()
    }

    /**
     * Start safety simulation mode moving towards destination.
     * Helpful for testing geofence alarms indoors.
     */
    fun startSimulation(destination: LocationPoint, alertRadiusMeters: Int) {
        stopLocationUpdates()
        stopSimulation()

        simulationJob = scope.launch {
            // Start 3.0 km away from destination
            val startDistKm = 2.5
            val startLat = destination.latitude - (startDistKm * 0.009)
            val startLng = destination.longitude - (startDistKm * 0.012)
            var currentLat = startLat
            var currentLng = startLng

            val steps = 30
            val latStep = (destination.latitude - startLat) / steps
            val lngStep = (destination.longitude - startLng) / steps

            var step = 0
            while (isActive && step <= steps) {
                currentLat += latStep
                currentLng += lngStep

                val simulatedSpeed = 45f + (Math.random().toFloat() * 10f) // 45-55 km/h train/metro speed
                _userLocation.value = UserLocation(
                    latitude = currentLat,
                    longitude = currentLng,
                    accuracyMeters = 8f,
                    speedKmh = simulatedSpeed,
                    isGpsActive = true,
                    isSimulated = true
                )

                delay(1200)
                step++
            }
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    companion object {
        fun calculateDistanceMeters(
            fromLat: Double,
            fromLng: Double,
            toLat: Double,
            toLng: Double
        ): Float {
            val earthRadius = 6371000.0 // meters
            val dLat = Math.toRadians(toLat - fromLat)
            val dLng = Math.toRadians(toLng - fromLng)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(toLat)) *
                    Math.sin(dLng / 2) * Math.sin(dLng / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return (earthRadius * c).toFloat()
        }
    }
}
