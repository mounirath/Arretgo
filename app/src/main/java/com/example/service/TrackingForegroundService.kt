package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.FavoritePlace
import com.example.model.AlarmTone
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * TrackingForegroundService
 *
 * Runs persistent GPS tracking in the background with a foreground service notification.
 * Monitors:
 * 1. Active trip progress & destination perimeter arrival.
 * 2. Continuous proximity to all saved favorite transit stops from the Room database,
 *    triggering high-priority audible & visual notification alerts whenever the user
 *    is within a specified distance of a saved favorite stop.
 */
class TrackingForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var soundVibrationManager: SoundVibrationManager? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var destinationName: String = ""
    private var destLatitude: Double = 0.0
    private var destLongitude: Double = 0.0
    private var alertRadiusMeters: Int = 500

    // Favorite transit stops monitored continuously in the background
    @Volatile
    private var favoriteStops: List<FavoritePlace> = emptyList()
    private val alertedFavoriteIds = Collections.synchronizedSet(mutableSetOf<Long>())

    companion object {
        const val TAG = "TrackingService"
        const val CHANNEL_ID = "arriva_tracking_channel"
        const val CHANNEL_FAVORITE_ALERT_ID = "arriva_favorite_alert_channel"
        const val NOTIFICATION_ID = 2026
        const val NOTIFICATION_FAVORITE_BASE_ID = 3000

        const val ACTION_START = "com.example.service.START_TRACKING"
        const val ACTION_START_MONITORING_FAVORITES = "com.example.service.START_MONITORING_FAVORITES"
        const val ACTION_STOP = "com.example.service.STOP_TRACKING"
        const val ACTION_DISMISS_FAVORITE_ALERT = "com.example.service.DISMISS_FAVORITE_ALERT"

        const val EXTRA_DEST_NAME = "extra_dest_name"
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"
        const val EXTRA_RADIUS = "extra_radius"
        const val EXTRA_FAV_ID = "extra_fav_id"
        const val EXTRA_FOCUS_FAVORITE_ID = "extra_focus_favorite_id"

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _monitoredFavoritesCount = MutableStateFlow(0)
        val monitoredFavoritesCount: StateFlow<Int> = _monitoredFavoritesCount.asStateFlow()

        private val _activeFavoriteAlert = MutableStateFlow<FavoritePlace?>(null)
        val activeFavoriteAlert: StateFlow<FavoritePlace?> = _activeFavoriteAlert.asStateFlow()

        /**
         * Start tracking for an active trip destination while simultaneously monitoring favorite stops
         */
        fun start(context: Context, destName: String, lat: Double, lng: Double, radiusMeters: Int) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DEST_NAME, destName)
                putExtra(EXTRA_LAT, lat)
                putExtra(EXTRA_LNG, lng)
                putExtra(EXTRA_RADIUS, radiusMeters)
            }
            startServiceCompat(context, intent)
        }

        /**
         * Start background monitoring service specifically for all saved favorite transit stops
         */
        fun startFavoriteMonitoring(context: Context) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_START_MONITORING_FAVORITES
            }
            startServiceCompat(context, intent)
        }

        /**
         * Stop the background tracking service
         */
        fun stop(context: Context) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }

        /**
         * Dismiss sound/vibration alert for a specific favorite stop
         */
        fun dismissFavoriteAlert(context: Context, favId: Long) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_DISMISS_FAVORITE_ALERT
                putExtra(EXTRA_FAV_ID, favId)
            }
            context.startService(intent)
        }

        private fun startServiceCompat(context: Context, intent: Intent) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting TrackingForegroundService", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        _isServiceActive.value = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        soundVibrationManager = SoundVibrationManager(this)
        createNotificationChannels()

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Arriva:GpsTrackingWakeLock")?.apply {
            setReferenceCounted(false)
        }

        // Continually observe favorite transit stops from Room database
        serviceScope.launch {
            try {
                AppDatabase.getInstance(applicationContext).favoriteDao().getAllFavorites()
                    .collect { stops ->
                        favoriteStops = stops
                        _monitoredFavoritesCount.value = stops.size
                        Log.d(TAG, "Room favorites synchronized: ${stops.size} stops monitored")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load favorites in background service", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_DISMISS_FAVORITE_ALERT -> {
                val favId = intent.getLongExtra(EXTRA_FAV_ID, -1L)
                if (favId != -1L) {
                    alertedFavoriteIds.add(favId)
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel((NOTIFICATION_FAVORITE_BASE_ID + favId).toInt())
                }
                soundVibrationManager?.stopAlarmSound()
                soundVibrationManager?.stopVibration()
                _activeFavoriteAlert.value = null
                return START_STICKY
            }
            ACTION_START -> {
                destinationName = intent.getStringExtra(EXTRA_DEST_NAME) ?: "Destination"
                destLatitude = intent.getDoubleExtra(EXTRA_LAT, 0.0)
                destLongitude = intent.getDoubleExtra(EXTRA_LNG, 0.0)
                alertRadiusMeters = intent.getIntExtra(EXTRA_RADIUS, 500)
                startTrackingForeground()
            }
            ACTION_START_MONITORING_FAVORITES -> {
                // Background monitoring mode without a single specific trip destination
                destinationName = ""
                destLatitude = 0.0
                destLongitude = 0.0
                startTrackingForeground()
            }
            else -> {
                startTrackingForeground()
            }
        }
        return START_STICKY
    }

    private fun startTrackingForeground() {
        try {
            wakeLock?.acquire(4 * 60 * 60 * 1000L) // 4 hours safety limit
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock acquire error", e)
        }

        val initialText = if (destLatitude != 0.0 && destLongitude != 0.0) {
            "Trajet vers $destinationName • Calcul de distance..."
        } else {
            "Surveillance active • ${favoriteStops.size} arrêts favoris sous veille GPS"
        }

        val notification = buildOngoingNotification(initialText, isAlert = false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1500L)
            .setMinUpdateDistanceMeters(2f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                handleNewLocation(loc)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing location permissions for foreground service", e)
        }
    }

    private fun handleNewLocation(location: Location) {
        val hasTripDest = destLatitude != 0.0 && destLongitude != 0.0
        var isWithinTripZone = false
        var tripDistMeters = 0f

        // 1. Process active trip destination if one is defined
        if (hasTripDest) {
            val tripResults = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                destLatitude,
                destLongitude,
                tripResults
            )
            tripDistMeters = tripResults[0]
            isWithinTripZone = tripDistMeters <= alertRadiusMeters

            val formattedDist = formatDistance(tripDistMeters)
            val text = if (isWithinTripZone) {
                "🚨 Dans la zone d'alerte ! ($formattedDist restant)"
            } else {
                "Distance restante : $formattedDist (Réveil à $alertRadiusMeters m)"
            }

            val notification = buildOngoingNotification(text, isAlert = isWithinTripZone)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        } else {
            val stopCount = favoriteStops.size
            val text = "Surveillance active de $stopCount arrêt(s) favori(s) en temps réel"
            val notification = buildOngoingNotification(text, isAlert = false)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }

        // 2. Continuous Proximity Detection for ALL Saved Favorite Transit Stops
        checkFavoritesProximity(location)
    }

    /**
     * Checks distance between user location and each saved favorite transit stop.
     * When within custom alert radius, triggers heads-up alert notification, audio and vibration.
     */
    private fun checkFavoritesProximity(location: Location) {
        val results = FloatArray(1)
        val currentStops = favoriteStops

        for (fav in currentStops) {
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                fav.latitude,
                fav.longitude,
                results
            )
            val dist = results[0]
            val threshold = fav.defaultRadiusMeters.toFloat()

            // If user moved far away (> 1.5x threshold), reset acknowledged state for future arrival
            if (dist > threshold * 1.5f) {
                alertedFavoriteIds.remove(fav.id)
            }

            // Trigger notification alert if user entered the stop's alert radius
            if (dist <= threshold && !alertedFavoriteIds.contains(fav.id)) {
                alertedFavoriteIds.add(fav.id)
                triggerFavoriteAlertNotification(fav, dist)
            }
        }
    }

    private fun triggerFavoriteAlertNotification(fav: FavoritePlace, distMeters: Float) {
        val formattedDist = formatDistance(distMeters)
        Log.i(TAG, "Proximity alert triggered for favorite stop '${fav.name}' at $formattedDist")

        _activeFavoriteAlert.value = fav

        // Sound & Vibration alert
        try {
            soundVibrationManager?.startAlarmSound(AlarmTone.SIREN)
            soundVibrationManager?.startVibration()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing proximity alert sound/vibration", e)
        }

        // Open app pending intent
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_FOCUS_FAVORITE_ID, fav.id)
        }
        val pendingOpen = PendingIntent.getActivity(
            this,
            (fav.id * 10).toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss alert pending intent
        val dismissIntent = Intent(this, TrackingForegroundService::class.java).apply {
            action = ACTION_DISMISS_FAVORITE_ALERT
            putExtra(EXTRA_FAV_ID, fav.id)
        }
        val pendingDismiss = PendingIntent.getService(
            this,
            (fav.id * 10 + 1).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🚨 Arrêt proche : ${fav.name} !"
        val message = "Vous êtes à $formattedDist de votre arrêt (${fav.tag} • seuil ${fav.defaultRadiusMeters} m). Préparez-vous à descendre !"

        val alertNotification = NotificationCompat.Builder(this, CHANNEL_FAVORITE_ALERT_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(pendingOpen)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Arrêter l'alerte", pendingDismiss)
            .addAction(android.R.drawable.ic_menu_mapmode, "Voir l'arrêt", pendingOpen)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 800))
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (NOTIFICATION_FAVORITE_BASE_ID + fav.id).toInt()
        manager.notify(notificationId, alertNotification)
    }

    private fun buildOngoingNotification(contentText: String, isAlert: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TrackingForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isAlert) {
            "🔔 RÉVEILLEZ-VOUS ! $destinationName"
        } else if (destinationName.isNotBlank()) {
            "arreva GPS • Suivi vers $destinationName"
        } else {
            "arreva GPS • Surveillance des arrêts favoris"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(!isAlert)
            .setAutoCancel(isAlert)
            .setPriority(if (isAlert) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingOpen)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Arrêter", pendingStop)
            .build()
    }

    private fun formatDistance(distance: Float): String {
        return if (distance >= 1000) {
            String.format("%.1f km", distance / 1000f)
        } else {
            "${distance.toInt()} m"
        }
    }

    private fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        try {
            soundVibrationManager?.stopAlarmSound()
            soundVibrationManager?.stopVibration()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping sound/vibration", e)
        }
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing wake lock", e)
        }
        _isServiceActive.value = false
        _activeFavoriteAlert.value = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // 1. Ongoing GPS Tracking Channel
            val trackingChannel = NotificationChannel(
                CHANNEL_ID,
                "Suivi GPS d'arrivée Arriva",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications actives de surveillance GPS en arrière-plan"
                setShowBadge(false)
            }
            manager.createNotificationChannel(trackingChannel)

            // 2. High-Priority Favorite Proximity Alarm Channel
            val alertChannel = NotificationChannel(
                CHANNEL_FAVORITE_ALERT_ID,
                "Alertes d'arrêts favoris Arriva",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes prioritaires à l'approche des arrêts et stations favorites"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                setShowBadge(true)
            }
            manager.createNotificationChannel(alertChannel)
        }
    }

    override fun onDestroy() {
        stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
