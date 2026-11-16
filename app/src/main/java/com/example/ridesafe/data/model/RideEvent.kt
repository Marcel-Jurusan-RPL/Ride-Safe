package com.example.ridesafe.data.model

data class RideEvent(
    val id: Long = 0,
    val tripId: Long,
    val timestamp: Long,
    val eventType: String,
    val severity: String,
    val sensorMagnitude: Float,
    val description: String
)
