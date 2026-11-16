package com.example.ridesafe.data.repository

import com.example.ridesafe.data.database.RideEventDao
import com.example.ridesafe.data.database.TripDao
import com.example.ridesafe.data.model.RideEvent
import com.example.ridesafe.data.model.TripSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RideRepository(
    private val tripDao: TripDao,
    private val eventDao: RideEventDao
) {
    suspend fun getAllTrips(): List<TripSession> = withContext(Dispatchers.IO) {
        tripDao.getAllTrips()
    }

    suspend fun getTripById(id: Long): TripSession? = withContext(Dispatchers.IO) {
        tripDao.getTripById(id)
    }

    suspend fun saveTripWithEvents(trip: TripSession, events: List<RideEvent>): Long = withContext(Dispatchers.IO) {
        val tripId = tripDao.insertTrip(trip)
        events.forEach { event ->
            eventDao.insertEvent(event.copy(tripId = tripId))
        }
        tripId
    }

    suspend fun deleteTrip(id: Long): Boolean = withContext(Dispatchers.IO) {
        eventDao.deleteEventsForTrip(id)
        tripDao.deleteTrip(id) > 0
    }

    suspend fun getEventsForTrip(tripId: Long): List<RideEvent> = withContext(Dispatchers.IO) {
        eventDao.getEventsForTrip(tripId)
    }

    suspend fun clearAllData(): Unit = withContext(Dispatchers.IO) {
        tripDao.deleteAllTrips()
    }
}
