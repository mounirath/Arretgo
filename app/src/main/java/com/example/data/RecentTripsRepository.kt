package com.example.data

import kotlinx.coroutines.flow.Flow

class RecentTripsRepository(private val dao: RecentTripDao) {

    val allRecentTrips: Flow<List<RecentTrip>> = dao.getAllRecentTrips()

    suspend fun recordTrip(trip: RecentTrip): Long {
        return dao.insertTrip(trip)
    }

    suspend fun deleteTrip(trip: RecentTrip) {
        dao.deleteTrip(trip)
    }

    suspend fun clearHistory() {
        dao.clearAllTrips()
    }
}
