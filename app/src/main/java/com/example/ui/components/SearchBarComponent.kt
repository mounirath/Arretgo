package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.LocationPoint
import com.example.model.MapStyle
import com.example.model.UserLocation
import com.example.ui.theme.ArrevaDarkTokens

/**
 * Top Header & Floating Search Bar matching Screenshot 1:
 * - [≡] Hamburger button in rounded square dark navy container
 * - Compass logo in glowing orange/amber circular disc
 * - [• GPS Actif] capsule
 * - [FR | EN | عر] segmented pill with active amber tab
 * - [T A] text size button & [🗺] map layers button
 * - Floating dark search bar with prompt tooltip: "pour placer votre destination"
 */
@Composable
fun GoogleMapsTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<LocationPoint>,
    isSearching: Boolean,
    onSelectPlace: (LocationPoint) -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit = {},
    currentMapStyle: MapStyle,
    onMapStyleChange: (MapStyle) -> Unit,
    userLocation: UserLocation,
    onOpenMenu: () -> Unit,
    onOpenMapLayers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // =========================================================================
        // TOP APP BAR (Exact match to Screenshot 1)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Hamburger Menu Button [≡]
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ArrevaDarkTokens.NavyCard)
                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenMenu)
                    .testTag("btn_menu"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = ArrevaDarkTokens.TextLightSlate,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 2. Compass Logo (Orange/Amber Glowing Disc)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(8.dp, CircleShape, spotColor = ArrevaDarkTokens.AmberLight)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                ArrevaDarkTokens.AmberGlow,
                                ArrevaDarkTokens.AmberLight,
                                ArrevaDarkTokens.AmberPrimary
                            )
                        )
                    )
                    .clickable(onClick = onOpenMenu),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "arreva GPS",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3. [• GPS Actif] Capsule
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ArrevaDarkTokens.EmeraldBg)
                    .border(BorderStroke(1.dp, ArrevaDarkTokens.EmeraldGps.copy(alpha = 0.5f)), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .shadow(4.dp, CircleShape, spotColor = ArrevaDarkTokens.EmeraldGps)
                            .clip(CircleShape)
                            .background(ArrevaDarkTokens.EmeraldGps)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GPS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArrevaDarkTokens.EmeraldGps,
                            lineHeight = 12.sp
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.AR -> "نشط"
                                AppLanguage.EN -> "Active"
                                AppLanguage.FR -> "Actif"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ArrevaDarkTokens.EmeraldGps,
                            lineHeight = 11.sp
                        )
                    }
                }
            }

            // 4. [FR | EN | عر] Language Switcher Segmented Pill
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ArrevaDarkTokens.NavyCard)
                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(20.dp))
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // FR Tab
                    val isFr = currentLanguage == AppLanguage.FR
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isFr) ArrevaDarkTokens.AmberLight else Color.Transparent)
                            .clickable { onLanguageChange(AppLanguage.FR) }
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFr) ArrevaDarkTokens.TextOnAmber else ArrevaDarkTokens.TextSecondary
                        )
                    }

                    // EN Tab
                    val isEn = currentLanguage == AppLanguage.EN
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isEn) ArrevaDarkTokens.AmberLight else Color.Transparent)
                            .clickable { onLanguageChange(AppLanguage.EN) }
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "EN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEn) ArrevaDarkTokens.TextOnAmber else ArrevaDarkTokens.TextSecondary
                        )
                    }

                    // AR Tab
                    val isAr = currentLanguage == AppLanguage.AR
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isAr) ArrevaDarkTokens.AmberLight else Color.Transparent)
                            .clickable { onLanguageChange(AppLanguage.AR) }
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "عر",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAr) ArrevaDarkTokens.TextOnAmber else ArrevaDarkTokens.TextSecondary
                        )
                    }
                }
            }

            // 5. [T A] Text Display Mode Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ArrevaDarkTokens.NavyCard)
                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "T",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArrevaDarkTokens.AmberLight
                    )
                    Text(
                        text = "A",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArrevaDarkTokens.TextLightSlate
                    )
                }
            }

            // 6. [🗺] Map Layers Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ArrevaDarkTokens.NavyCard)
                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenMapLayers)
                    .testTag("btn_top_map_layers"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Map Style",
                    tint = ArrevaDarkTokens.TextLightSlate,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // =========================================================================
        // FLOATING SEARCH BAR & PROMPT BUBBLE (Exact match to Screenshot 1)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Floating Search Capsule
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(26.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(26.dp))
                        .background(ArrevaDarkTokens.NavyCard.copy(alpha = 0.95f))
                        .border(BorderStroke(1.25.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(26.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ArrevaDarkTokens.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        TextField(
                            value = query,
                            onValueChange = onQueryChange,
                            placeholder = {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.AR -> "ابحث عن محطة، موقف أو عنوان..."
                                        AppLanguage.EN -> "Search station, stop or address..."
                                        AppLanguage.FR -> "Rechercher une gare, arrêt ou adresse..."
                                    },
                                    color = ArrevaDarkTokens.TextSecondary,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = ArrevaDarkTokens.TextPrimary,
                                unfocusedTextColor = ArrevaDarkTokens.TextLightSlate
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_input_field")
                        )

                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(end = 4.dp),
                                strokeWidth = 2.dp,
                                color = ArrevaDarkTokens.AmberLight
                            )
                        } else if (query.isNotBlank()) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = ArrevaDarkTokens.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Speech Bubble Prompt: "pour placer votre destination" (as seen in Screenshot 1)
                if (query.isBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .shadow(8.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(ArrevaDarkTokens.NavySurface.copy(alpha = 0.94f))
                            .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(18.dp))
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.AR -> "أو انقر على الخريطة لتحديد وجهتك"
                                AppLanguage.EN -> "or tap map to place destination"
                                AppLanguage.FR -> "pour placer votre destination"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ArrevaDarkTokens.TextLightSlate
                        )
                    }
                }
            }
        }

        // Search Results Dropdown List
        AnimatedVisibility(
            visible = searchResults.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .shadow(16.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(ArrevaDarkTokens.NavyCard)
                    .border(BorderStroke(1.dp, ArrevaDarkTokens.NavyBorder), RoundedCornerShape(18.dp))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    items(searchResults) { place ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectPlace(place) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ArrevaDarkTokens.AmberPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ArrevaDarkTokens.AmberLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ArrevaDarkTokens.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (place.address.isNotBlank()) {
                                    Text(
                                        text = place.address,
                                        fontSize = 12.sp,
                                        color = ArrevaDarkTokens.TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
