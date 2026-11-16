package com.example.ridesafe.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

data class LightStatus(
    val lux: Float = 0f,
    val condition: String = "Normal",
    val isTunnelOrDark: Boolean = false,
    val description: String = "Pencahayaan memadai"
)

class EnvironmentLightTracker(
    context: Context,
    private val onLightChanged: (LightStatus) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    val isAvailable: Boolean
        get() = lightSensor != null

    fun start() {
        lightSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_LIGHT) return

        val lux = event.values[0]
        val status = classifyLight(lux)
        onLightChanged(status)
    }

    fun classifyLight(lux: Float): LightStatus {
        return when {
            lux < 25f -> LightStatus(
                lux = lux,
                condition = "Gelap / Terowongan",
                isTunnelOrDark = true,
                description = "Lampu utama direkomendasikan menyala. Mode Gelap Otomatis Aktif."
            )
            lux in 25f..<120f -> LightStatus(
                lux = lux,
                condition = "Redup / Senja",
                isTunnelOrDark = true,
                description = "Kondisi cahaya mulai minim. Mode Gelap Aktif."
            )
            lux in 120f..<1000f -> LightStatus(
                lux = lux,
                condition = "Mendung / Teduh",
                isTunnelOrDark = false,
                description = "Pencahayaan cukup baik."
            )
            lux in 1000f..<10000f -> LightStatus(
                lux = lux,
                condition = "Cerah / Siang Hari",
                isTunnelOrDark = false,
                description = "Kondisi jalanan sangat terang."
            )
            else -> LightStatus(
                lux = lux,
                condition = "Terik Matahari",
                isTunnelOrDark = false,
                description = "Sinar matahari langsung. Layar kontras tinggi disarankan."
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
