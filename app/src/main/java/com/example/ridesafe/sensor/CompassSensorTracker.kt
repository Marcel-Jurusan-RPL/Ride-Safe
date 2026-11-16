package com.example.ridesafe.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CompassSensorTracker(
    context: Context,
    private val onHeadingChanged: (degrees: Float, cardinalDirection: String) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var smoothedHeading = 0f
    private val alpha = 0.15f // low pass filter factor

    val isAvailable: Boolean
        get() = accelerometer != null && magneticField != null

    fun start() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magneticField?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        hasGravity = false
        hasGeomagnetic = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, 3)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthInRadians = orientation[0]
                var azimuthDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                if (azimuthDegrees < 0) {
                    azimuthDegrees += 360f
                }

                // Smooth circular heading
                val radDiff = Math.toRadians((azimuthDegrees - smoothedHeading).toDouble())
                val sinDiff = sin(radDiff).toFloat()
                val cosDiff = cos(radDiff).toFloat()
                val deltaAngle = Math.toDegrees(atan2(sinDiff.toDouble(), cosDiff.toDouble())).toFloat()

                smoothedHeading = (smoothedHeading + deltaAngle * alpha + 360f) % 360f

                val cardinal = getCardinalDirection(smoothedHeading)
                onHeadingChanged(smoothedHeading, cardinal)
            }
        }
    }

    private fun getCardinalDirection(degrees: Float): String {
        return when {
            degrees >= 337.5f || degrees < 22.5f -> "Utara (N)"
            degrees in 22.5f..<67.5f -> "Timur Laut (NE)"
            degrees in 67.5f..<112.5f -> "Timur (E)"
            degrees in 112.5f..<157.5f -> "Tenggara (SE)"
            degrees in 157.5f..<202.5f -> "Selatan (S)"
            degrees in 202.5f..<247.5f -> "Barat Daya (SW)"
            degrees in 247.5f..<292.5f -> "Barat (W)"
            else -> "Barat Laut (NW)"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
