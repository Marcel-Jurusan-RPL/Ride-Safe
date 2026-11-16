package com.example.ridesafe.data.model

data class TripSession(
    val id: Long = 0,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val bumpCount: Int,
    val harshBrakeCount: Int,
    val safetyScore: Int,
    val safetyRating: String,
    val notes: String = ""
)
