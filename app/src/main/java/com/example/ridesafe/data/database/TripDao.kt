package com.example.ridesafe.data.database

import android.content.ContentValues
import android.database.Cursor
import com.example.ridesafe.data.model.TripSession

class TripDao(private val dbHelper: RideSafeDbHelper) {

    fun insertTrip(trip: TripSession): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(RideSafeDbHelper.COL_TRIP_TITLE, trip.title)
            put(RideSafeDbHelper.COL_TRIP_START_TIME, trip.startTime)
            put(RideSafeDbHelper.COL_TRIP_END_TIME, trip.endTime)
            put(RideSafeDbHelper.COL_TRIP_DURATION, trip.durationSeconds)
            put(RideSafeDbHelper.COL_TRIP_BUMP_COUNT, trip.bumpCount)
            put(RideSafeDbHelper.COL_TRIP_HARSH_BRAKE_COUNT, trip.harshBrakeCount)
            put(RideSafeDbHelper.COL_TRIP_SAFETY_SCORE, trip.safetyScore)
            put(RideSafeDbHelper.COL_TRIP_SAFETY_RATING, trip.safetyRating)
            put(RideSafeDbHelper.COL_TRIP_NOTES, trip.notes)
        }
        return db.insert(RideSafeDbHelper.TABLE_TRIPS, null, values)
    }

    fun getAllTrips(): List<TripSession> {
        val trips = mutableListOf<TripSession>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            RideSafeDbHelper.TABLE_TRIPS,
            null,
            null,
            null,
            null,
            null,
            "${RideSafeDbHelper.COL_TRIP_START_TIME} DESC"
        )

        cursor.use { c ->
            val idCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_ID)
            val titleCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_TITLE)
            val startCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_START_TIME)
            val endCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_END_TIME)
            val durCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_DURATION)
            val bumpCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_BUMP_COUNT)
            val brakeCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_HARSH_BRAKE_COUNT)
            val scoreCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_SAFETY_SCORE)
            val ratingCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_SAFETY_RATING)
            val notesCol = c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_NOTES)

            while (c.moveToNext()) {
                trips.add(
                    TripSession(
                        id = c.getLong(idCol),
                        title = c.getString(titleCol),
                        startTime = c.getLong(startCol),
                        endTime = c.getLong(endCol),
                        durationSeconds = c.getLong(durCol),
                        bumpCount = c.getInt(bumpCol),
                        harshBrakeCount = c.getInt(brakeCol),
                        safetyScore = c.getInt(scoreCol),
                        safetyRating = c.getString(ratingCol),
                        notes = c.getString(notesCol) ?: ""
                    )
                )
            }
        }
        return trips
    }

    fun getTripById(id: Long): TripSession? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            RideSafeDbHelper.TABLE_TRIPS,
            null,
            "${RideSafeDbHelper.COL_TRIP_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        return cursor.use { c ->
            if (c.moveToFirst()) {
                TripSession(
                    id = c.getLong(c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_ID)),
                    title = c.getString(c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_TITLE)),
                    startTime = c.getLong(c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_START_TIME)),
                    endTime = c.getLong(c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_END_TIME)),
                    durationSeconds = c.getLong(c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_DURATION)),
                    bumpCount = c.getInt(c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_BUMP_COUNT)),
                    harshBrakeCount = c.getInt(c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_HARSH_BRAKE_COUNT)),
                    safetyScore = c.getInt(c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_SAFETY_SCORE)),
                    safetyRating = c.getString(c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_SAFETY_RATING)),
                    notes = c.getString(c.getColumnIndexOrThrow(RideSafeDbHelper.COL_TRIP_NOTES)) ?: ""
                )
            } else null
        }
    }

    fun deleteTrip(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            RideSafeDbHelper.TABLE_TRIPS,
            "${RideSafeDbHelper.COL_TRIP_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun deleteAllTrips(): Int {
        val db = dbHelper.writableDatabase
        return db.delete(RideSafeDbHelper.TABLE_TRIPS, null, null)
    }
}
