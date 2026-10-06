package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [FavoritePlace::class, RecentTrip::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentTripDao(): RecentTripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arriva_database.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate with essential starter transit stops and sample recent trips
                            CoroutineScope(Dispatchers.IO).launch {
                                val favDao = getInstance(context).favoriteDao()
                                favDao.insertFavorite(
                                    FavoritePlace(
                                        name = "Paris - Gare de Lyon",
                                        address = "Place Louis-Armand, 75012 Paris",
                                        latitude = 48.8448,
                                        longitude = 2.3735,
                                        tag = "Gare",
                                        defaultRadiusMeters = 500
                                    )
                                )
                                favDao.insertFavorite(
                                    FavoritePlace(
                                        name = "Alger - Gare d'Agha",
                                        address = "Boulevard Mohamed V, Sidi M'Hamed",
                                        latitude = 36.7628,
                                        longitude = 3.0583,
                                        tag = "Gare",
                                        defaultRadiusMeters = 500
                                    )
                                )
                                favDao.insertFavorite(
                                    FavoritePlace(
                                        name = "Châtelet - Les Halles",
                                        address = "Forum des Halles, Paris",
                                        latitude = 48.8614,
                                        longitude = 2.3470,
                                        tag = "Métro",
                                        defaultRadiusMeters = 300
                                    )
                                )

                                val tripDao = getInstance(context).recentTripDao()
                                tripDao.insertTrip(
                                    RecentTrip(
                                        destinationName = "Alger - Gare d'Agha",
                                        destinationAddress = "Boulevard Mohamed V, Sidi M'Hamed",
                                        latitude = 36.7628,
                                        longitude = 3.0583,
                                        alertRadiusMeters = 500,
                                        initialDistanceMeters = 3200f,
                                        completedAt = System.currentTimeMillis() - 3600000 * 2, // 2 hours ago
                                        wasArrivalAlertTriggered = true
                                    )
                                )
                                tripDao.insertTrip(
                                    RecentTrip(
                                        destinationName = "Bab Ezzouar - Centre Commercial",
                                        destinationAddress = "Bab Ezzouar, Alger",
                                        latitude = 36.7231,
                                        longitude = 3.1812,
                                        alertRadiusMeters = 400,
                                        initialDistanceMeters = 8500f,
                                        completedAt = System.currentTimeMillis() - 3600000 * 24, // Yesterday
                                        wasArrivalAlertTriggered = true
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
