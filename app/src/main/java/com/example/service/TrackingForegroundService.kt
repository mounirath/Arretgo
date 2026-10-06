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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * TrackingForegroundService
 *
 * Runs persistent GPS tracking in the background with a foreground notification
 * so Arriva can monitor trip progress and sound the alarm even when the app is minimized,
 * locked, or running in background.
 */
class TrackingForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var destinationName: String = ""
    private var destLatitude: Double = 0.0
    private var destLongitude: Double = 0.0
    private var alertRadiusMeters: Int = 500

    companion object {
        const val CHANNEL_ID = "arriva_tracking_channel"
        const val NOTIFICATION_ID = 2026
        const val ACTION_START = "com.example.service.START_TRACKING"
        const val ACTION_STOP = "com.example.service.STOP_TRACKING"

        const val EXTRA_DEST_NAME = "extra_dest_name"
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"
        const val EXTRA_RADIUS = "extra_radius"

        fun start(context: Context, destName: String, lat: Double, lng: Double, radiusMeters: Int) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DEST_NAME, destName)
                putExtra(EXTRA_LAT, lat)
                putExtra(EXTRA_LNG, lng)
                putExtra(EXTRA_RADIUS, radiusMeters)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Arriva:GpsTrackingWakeLock")?.apply {
            setReferenceCounted(false)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                destinationName = intent.getStringExtra(EXTRA_DEST_NAME) ?: "Destination"
                destLatitude = intent.getDoubleExtra(EXTRA_LAT, 0.0)
                destLongitude = intent.getDoubleExtra(EXTRA_LNG, 0.0)
                alertRadiusMeters = intent.getIntExtra(EXTRA_RADIUS, 500)

                startTrackingForeground()
            }
        }
        return START_STICKY
    }

    private fun startTrackingForeground() {
        wakeLock?.acquire(4 * 60 * 60 * 1000L) // 4 hours safety limit

        val notification = buildNotification("Calcul de la distance...", isAlert = false)
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
            Log.e("TrackingService", "Missing location permissions for foreground service", e)
        }
    }

    private fun handleNewLocation(location: Location) {
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            destLatitude,
            destLongitude,
            results
        )
        val distance = results[0]

        val isWithinZone = distance <= alertRadiusMeters
        val formattedDist = if (distance >= 1000) {
            String.format("%.1f km", distance / 1000f)
        } else {
            "${distance.toInt()} m"
        }

        val text = if (isWithinZone) {
            "🚨 Dans la zone d'alerte ! ($formattedDist restant)"
        } else {
            "Distance restante : $formattedDist (Réveil à $alertRadiusMeters m)"
        }

        val notification = buildNotification(text, isAlert = isWithinZone)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(contentText: String, isAlert: Boolean): Notification {
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

        val title = if (isAlert) "🔔 RÉVEILLEZ-VOUS ! $destinationName" else "arreva GPS • Suivi vers $destinationName"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(!isAlert)
            .setAutoCancel(isAlert)
            .setPriority(if (isAlert) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingOpen)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Arrêter", pendingStop)
            .build()
    }

    private fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w("TrackingService", "Error releasing wake lock", e)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Suivi GPS d'arrivée Arriva",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications actives pendant le trajet pour réveil à l'approche"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopTracking()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
