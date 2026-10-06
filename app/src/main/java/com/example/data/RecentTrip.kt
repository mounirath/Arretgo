package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a trip completed or taken by the user.
 */
@Entity(tableName = "recent_trips")
data class RecentTrip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val destinationName: String,
    val destinationAddress: String = "",
    val latitude: Double,
    val longitude: Double,
    val alertRadiusMeters: Int = 500,
    val initialDistanceMeters: Float = 0f,
    val completedAt: Long = System.currentTimeMillis(),
    val wasArrivalAlertTriggered: Boolean = true
)
