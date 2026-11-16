package com.example.ridesafe.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RideSafeDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "ridesafe.db"
        const val DATABASE_VERSION = 1

        // Table Trips
        const val TABLE_TRIPS = "trips"
        const val COL_TRIP_ID = "id"
        const val COL_TRIP_TITLE = "title"
        const val COL_TRIP_START_TIME = "start_time"
        const val COL_TRIP_END_TIME = "end_time"
        const val COL_TRIP_DURATION = "duration_seconds"
        const val COL_TRIP_BUMP_COUNT = "bump_count"
        const val COL_TRIP_HARSH_BRAKE_COUNT = "harsh_brake_count"
        const val COL_TRIP_SAFETY_SCORE = "safety_score"
        const val COL_TRIP_SAFETY_RATING = "safety_rating"
        const val COL_TRIP_NOTES = "notes"

        // Table Ride Events
        const val TABLE_EVENTS = "ride_events"
        const val COL_EVENT_ID = "id"
        const val COL_EVENT_TRIP_ID = "trip_id"
        const val COL_EVENT_TIMESTAMP = "timestamp"
        const val COL_EVENT_TYPE = "event_type"
        const val COL_EVENT_SEVERITY = "severity"
        const val COL_EVENT_MAGNITUDE = "sensor_magnitude"
        const val COL_EVENT_DESCRIPTION = "description"

        private const val CREATE_TABLE_TRIPS = """
            CREATE TABLE $TABLE_TRIPS (
                $COL_TRIP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TRIP_TITLE TEXT NOT NULL,
                $COL_TRIP_START_TIME INTEGER NOT NULL,
                $COL_TRIP_END_TIME INTEGER NOT NULL,
                $COL_TRIP_DURATION INTEGER NOT NULL,
                $COL_TRIP_BUMP_COUNT INTEGER NOT NULL,
                $COL_TRIP_HARSH_BRAKE_COUNT INTEGER NOT NULL,
                $COL_TRIP_SAFETY_SCORE INTEGER NOT NULL,
                $COL_TRIP_SAFETY_RATING TEXT NOT NULL,
                $COL_TRIP_NOTES TEXT
            );
        """

        private const val CREATE_TABLE_EVENTS = """
            CREATE TABLE $TABLE_EVENTS (
                $COL_EVENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EVENT_TRIP_ID INTEGER NOT NULL,
                $COL_EVENT_TIMESTAMP INTEGER NOT NULL,
                $COL_EVENT_TYPE TEXT NOT NULL,
                $COL_EVENT_SEVERITY TEXT NOT NULL,
                $COL_EVENT_MAGNITUDE REAL NOT NULL,
                $COL_EVENT_DESCRIPTION TEXT NOT NULL,
                FOREIGN KEY($COL_EVENT_TRIP_ID) REFERENCES $TABLE_TRIPS($COL_TRIP_ID) ON DELETE CASCADE
            );
        """
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_TRIPS)
        db.execSQL(CREATE_TABLE_EVENTS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIPS")
        onCreate(db)
    }
}
