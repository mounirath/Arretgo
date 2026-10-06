package com.example.data

import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val favoriteDao: FavoriteDao) {
    val allFavorites: Flow<List<FavoritePlace>> = favoriteDao.getAllFavorites()

    suspend fun addFavorite(place: FavoritePlace): Long {
        return favoriteDao.insertFavorite(place)
    }

    suspend fun removeFavorite(place: FavoritePlace) {
        favoriteDao.deleteFavorite(place)
    }

    suspend fun removeFavoriteById(id: Long) {
        favoriteDao.deleteFavoriteById(id)
    }
}
