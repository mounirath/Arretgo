package com.example.model

import com.example.data.FavoritePlace

/**
 * Event triggered when the user approaches a geographic area saved as a favorite.
 */
data class FavoriteAlertEvent(
    val favorite: FavoritePlace,
    val distanceMeters: Float,
    val isRinging: Boolean = true,
    val isMuted: Boolean = false
)
