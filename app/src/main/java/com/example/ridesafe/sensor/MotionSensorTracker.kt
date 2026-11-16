package com.example.ridesafe.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

class MotionSensorTracker(
    context: Context,
    private val onBumpDetected: (magnitude: Float) -> Unit,
    private val onHarshBrakeDetected: (magnitude: Float) -> Unit,
    private val onMotionValues: (x: Float, y: Float, z: Float, magnitude: Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var bumpThreshold: Float = 6.0f // delta magnitude above gravity (m/s^2)
    var harshBrakeThreshold: Float = 5.5f // horizontal deceleration (m/s^2)

    private var lastBumpTime: Long = 0
    private var lastBrakeTime: Long = 0
    private val cooldownMs: Long = 800 // debounce

    val isAvailable: Boolean
        get() = accelerometer != null

    fun start() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val totalMag = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val deltaMag = abs(totalMag - SensorManager.GRAVITY_EARTH)

        onMotionValues(x, y, z, totalMag)

        val currentTime = System.currentTimeMillis()

        // Deteksi Guncangan / Jalan Berlubang (Pothole / Bump)
        if (deltaMag > bumpThreshold && (currentTime - lastBumpTime) > cooldownMs) {
            lastBumpTime = currentTime
            onBumpDetected(deltaMag)
        }

        // Deteksi Pengereman Mendadak (Harsh Braking)
        // Saat mengerem keras pada dudukan hp motor/mobil, komponen y/x mengalami deselerasi tajam
        if ((abs(y) > harshBrakeThreshold || (abs(x) > harshBrakeThreshold && deltaMag > 4.5f))
            && (currentTime - lastBrakeTime) > cooldownMs && (currentTime - lastBumpTime) > 300
        ) {
            lastBrakeTime = currentTime
            onHarshBrakeDetected(abs(y).coerceAtLeast(abs(x)))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
