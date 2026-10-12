package com.example

import android.app.Application
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

class ArrivaApplication : Application(), OnMapsSdkInitializedCallback {

    override fun onCreate() {
        super.onCreate()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val processName = Application.getProcessName()
            if (packageName != processName) {
                try {
                    android.webkit.WebView.setDataDirectorySuffix(processName)
                } catch (e: Exception) {
                    Log.w("ArrivaApplication", "Failed to set WebView data directory suffix: ${e.message}")
                }
            }
        }
        initializeGoogleMapsSdk()
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
