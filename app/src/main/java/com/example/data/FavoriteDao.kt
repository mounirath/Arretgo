package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_places ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoritePlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(place: FavoritePlace): Long

    @Delete
    suspend fun deleteFavorite(place: FavoritePlace)

    @Query("DELETE FROM favorite_places WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)

    @Query("SELECT COUNT(*) FROM favorite_places")
    suspend fun getCount(): Int
}
