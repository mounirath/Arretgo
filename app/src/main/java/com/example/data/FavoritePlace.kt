package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_places")
data class FavoritePlace(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String = "",
    val latitude: Double,
    val longitude: Double,
    val tag: String = "Gare", // Maison, Travail, Gare, Métro, etc.
    val defaultRadiusMeters: Int = 500,
    val createdAt: Long = System.currentTimeMillis()
)
