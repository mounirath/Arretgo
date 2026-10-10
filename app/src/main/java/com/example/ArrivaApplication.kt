package com.example

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

/**
 * ArrivaApplication - Application entry point.
 * Guarantees early initialization of Google Maps SDK and Google AdMob.
 */
class ArrivaApplication : Application(), OnMapsSdkInitializedCallback {

    override fun onCreate() {
        super.onCreate()
        initializeGoogleMapsSdk()
        initializeAdMob()
    }

    private fun initializeAdMob() {
        Thread {
            try {
                val requestConfiguration = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
                MobileAds.setRequestConfiguration(requestConfiguration)
                MobileAds.initialize(this) { status ->
                    Log.d("ArrivaApplication", "AdMob MobileAds initialized: $status")
                }
            } catch (e: Exception) {
                Log.w("ArrivaApplication", "Non-fatal AdMob MobileAds initialization exception", e)
            }
        }.start()
    }

    private fun initializeGoogleMapsSdk() {
        try {
            MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
            Log.d("ArrivaApplication", "Google Maps SDK LATEST initialization requested")
        } catch (e: Exception) {
            Log.w("ArrivaApplication", "Failed to initialize LATEST Maps renderer, trying LEGACY", e)
            try {
                MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LEGACY, this)
                Log.d("ArrivaApplication", "Google Maps SDK LEGACY initialization requested")
            } catch (ex: Exception) {
                Log.e("ArrivaApplication", "Failed to initialize Google Maps SDK with any renderer", ex)
            }
        }
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> {
                Log.i("ArrivaApplication", "Google Maps SDK successfully initialized with LATEST renderer.")
            }
            MapsInitializer.Renderer.LEGACY -> {
                Log.i("ArrivaApplication", "Google Maps SDK initialized with LEGACY renderer.")
            }
        }
    }
}
