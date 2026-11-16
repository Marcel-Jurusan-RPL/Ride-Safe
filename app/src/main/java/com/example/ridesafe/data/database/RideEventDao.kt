package com.example.ridesafe.data.database

import android.content.ContentValues
import android.database.Cursor
import com.example.ridesafe.data.model.RideEvent

class RideEventDao(private val dbHelper: RideSafeDbHelper) {

    fun insertEvent(event: RideEvent): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(RideSafeDbHelper.COL_EVENT_TRIP_ID, event.tripId)
            put(RideSafeDbHelper.COL_EVENT_TIMESTAMP, event.timestamp)
            put(RideSafeDbHelper.COL_EVENT_TYPE, event.eventType)
            put(RideSafeDbHelper.COL_EVENT_SEVERITY, event.severity)
            put(RideSafeDbHelper.COL_EVENT_MAGNITUDE, event.sensorMagnitude)
            put(RideSafeDbHelper.COL_EVENT_DESCRIPTION, event.description)
        }
        return db.insert(RideSafeDbHelper.TABLE_EVENTS, null, values)
    }

    fun getEventsForTrip(tripId: Long): List<RideEvent> {
        val events = mutableListOf<RideEvent>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            RideSafeDbHelper.TABLE_EVENTS,
            null,
            "${RideSafeDbHelper.COL_EVENT_TRIP_ID} = ?",
            arrayOf(tripId.toString()),
            null,
            null,
            "${RideSafeDbHelper.COL_EVENT_TIMESTAMP} ASC"
        )

        cursor.use { c ->
            val idCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_EVENT_ID)
            val tripIdCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_EVENT_TRIP_ID)
            val timeCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_EVENT_TIMESTAMP)
            val typeCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_EVENT_TYPE)
            val sevCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_EVENT_SEVERITY)
            val magCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_EVENT_MAGNITUDE)
            val descCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_EVENT_DESCRIPTION)

            while (c.moveToNext()) {
                events.add(
                    RideEvent(
                        id = c.getLong(idCol),
                        tripId = c.getLong(tripIdCol),
                        timestamp = c.getLong(timeCol),
                        eventType = c.getString(typeCol),
                        severity = c.getString(sevCol),
                        sensorMagnitude = c.getFloat(magCol),
                        description = c.getString(descCol)
                    )
                )
            }
        }
        return events
    }

    fun deleteEventsForTrip(tripId: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            RideSafeDbHelper.TABLE_EVENTS,
            "${RideSafeDbHelper.COL_EVENT_TRIP_ID} = ?",
            arrayOf(tripId.toString())
        )
    }
}
