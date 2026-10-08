package com.example.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * AppOpenAdManager
 *
 * Manages Google AdMob App Open Ads using the official Google Test Ad ID:
 * ca-app-pub-3940256099942544/9257395921
 */
class AppOpenAdManager(private val context: Context) {

    companion object {
        const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
        const val PROD_APP_OPEN_AD_UNIT_ID = "ca-app-pub-1050422776945344/9720135357"
    }

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Long = 0

    fun loadAd(adUnitId: String = PROD_APP_OPEN_AD_UNIT_ID) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d("AppOpenAdManager", "Google App Open Ad loaded successfully: $adUnitId")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    Log.w("AppOpenAdManager", "Google App Open Ad failed to load with $adUnitId: ${loadAdError.message}")
                    // Fallback to official test ad unit if live unit has no inventory
                    if (adUnitId != TEST_APP_OPEN_AD_UNIT_ID) {
                        Log.d("AppOpenAdManager", "Falling back to official Google test App Open Ad unit...")
                        loadAd(TEST_APP_OPEN_AD_UNIT_ID)
                    }
                }
            }
        )
    }

    private fun isAdAvailable(): Boolean {
        val wasLoadedRecently = (Date().time - loadTime) < 4 * 3600 * 1000
        return appOpenAd != null && wasLoadedRecently
    }

    fun showAdIfAvailable(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (isShowingAd) {
            return
        }

        if (!isAdAvailable()) {
            loadAd()
            onAdDismissed()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                loadAd()
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                loadAd()
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }

        isShowingAd = true
        appOpenAd?.show(activity)
    }
}
