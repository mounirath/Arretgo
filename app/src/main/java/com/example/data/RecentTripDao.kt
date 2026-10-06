package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentTripDao {

    @Query("SELECT * FROM recent_trips ORDER BY completedAt DESC LIMIT 50")
    fun getAllRecentTrips(): Flow<List<RecentTrip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: RecentTrip): Long

    @Delete
    suspend fun deleteTrip(trip: RecentTrip)

    @Query("DELETE FROM recent_trips")
    suspend fun clearAllTrips()
}
