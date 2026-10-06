package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [FavoritePlace::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

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
                            // Pre-populate with essential starter transit stops
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).favoriteDao()
                                dao.insertFavorite(
                                    FavoritePlace(
                                        name = "Paris - Gare de Lyon",
                                        address = "Place Louis-Armand, 75012 Paris",
                                        latitude = 48.8448,
                                        longitude = 2.3735,
                                        tag = "Gare",
                                        defaultRadiusMeters = 500
                                    )
                                )
                                dao.insertFavorite(
                                    FavoritePlace(
                                        name = "Alger - Gare d'Agha",
                                        address = "Boulevard Mohamed V, Sidi M'Hamed",
                                        latitude = 36.7628,
                                        longitude = 3.0583,
                                        tag = "Gare",
                                        defaultRadiusMeters = 500
                                    )
                                )
                                dao.insertFavorite(
                                    FavoritePlace(
                                        name = "Châtelet - Les Halles",
                                        address = "Forum des Halles, Paris",
                                        latitude = 48.8614,
                                        longitude = 2.3470,
                                        tag = "Métro",
                                        defaultRadiusMeters = 300
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
