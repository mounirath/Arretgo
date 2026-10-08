package com.example.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.ArrevaDarkTokens
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * AdMob Banner Component
 *
 * Official Google AdMob Test Banner ID: ca-app-pub-3940256099942544/9214589741
 * User Production Banner Unit ID: ca-app-pub-1050422776945344/9720135357
 * Alternative Unit IDs: ca-app-pub-1050422776945344/9881281832, ca-app-pub-1050422776945344/7585760755
 */
const val TEST_ADMOB_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
const val PROD_ADMOB_BANNER_AD_UNIT_ID = "ca-app-pub-1050422776945344/9720135357"

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = PROD_ADMOB_BANNER_AD_UNIT_ID
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var adError by remember { mutableStateOf<String?>(null) }
    var currentUnitId by remember(adUnitId) { mutableStateOf(adUnitId) }

    val adView = remember(currentUnitId) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            setAdUnitId(currentUnitId)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    isAdLoaded = true
                    adError = null
                    Log.d("AdMobBanner", "Google AdMob Banner loaded successfully: $currentUnitId")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.w("AdMobBanner", "AdMob banner load failed with $currentUnitId: ${error.code} - ${error.message}")
                    // In debug/development or if production ad has no inventory, automatically fall back to test banner unit
                    if (currentUnitId != TEST_ADMOB_BANNER_AD_UNIT_ID) {
                        Log.d("AdMobBanner", "Retrying banner with official Google test unit...")
                        currentUnitId = TEST_ADMOB_BANNER_AD_UNIT_ID
                    } else {
                        isAdLoaded = false
                        adError = error.message
                    }
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }

    // Arreva Dark Navy & Amber Framing
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(ArrevaDarkTokens.NavyCard)
            .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("admob_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Tag: SPONSORED // Google AdMob
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isAdLoaded) ArrevaDarkTokens.EmeraldGps else ArrevaDarkTokens.AmberLight)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ANNONCE // SPONSORED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = ArrevaDarkTokens.TextSecondary
                    )
                }

                Text(
                    text = "Google AdMob",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = ArrevaDarkTokens.TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // The AdView container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF020617)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { adView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                if (!isAdLoaded && adError == null) {
                    Text(
                        text = "Chargement de la publicité AdMob...",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = ArrevaDarkTokens.AmberLight.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
