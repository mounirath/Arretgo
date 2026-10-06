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
import com.example.ui.theme.Y2KTokens
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * AdMob Banner Component
 *
 * Configured with user's AdMob production credentials:
 * - App ID: ca-app-pub-1050422776945344~6855047295
 * - Ad Unit ID: ca-app-pub-1050422776945344/7585760755
 *
 * Designed with the Y2K digital aesthetic:
 * - Liquid-chrome framing
 * - Cyber telemetry badge [AD // SPONSORED]
 * - Graceful fallback & error handling
 */
const val ADMOB_BANNER_AD_UNIT_ID = "ca-app-pub-1050422776945344/7585760755"

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = ADMOB_BANNER_AD_UNIT_ID
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var adError by remember { mutableStateOf<String?>(null) }

    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            setAdUnitId(adUnitId)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    isAdLoaded = true
                    adError = null
                    Log.d("AdMobBanner", "Banner ad successfully loaded: $adUnitId")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    isAdLoaded = false
                    adError = error.message
                    Log.w("AdMobBanner", "Banner ad failed to load: ${error.code} - ${error.message}")
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

    // Y2K Liquid-Chrome Cyber Framing for the Ad Container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF00F0FF).copy(alpha = 0.25f),
                ambientColor = Color.White.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Y2KTokens.CyberChromeDark.copy(alpha = 0.95f))
            .border(BorderStroke(1.25.dp, Y2KTokens.ChromeBorderBrush), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("admob_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Y2K Monospace Telemetry Header
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
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isAdLoaded) Y2KTokens.TextCyanGlow else Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SPONSORED // AD",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Y2KTokens.TextMutedSteel
                    )
                }

                Text(
                    text = "Google AdMob",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // The AdView container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { adView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                // Placeholder / Loading indicator if ad is fetching
                if (!isAdLoaded && adError == null) {
                    Text(
                        text = "// CONNECTING ADMOB FEED //",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Y2KTokens.TextCyanGlow.copy(alpha = 0.70f)
                    )
                }
            }
        }
    }
}
