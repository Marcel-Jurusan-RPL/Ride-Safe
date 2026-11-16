package com.example.ridesafe.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesafe.data.model.RideEvent
import com.example.ridesafe.data.model.TripSession
import com.example.ridesafe.data.repository.RideRepository
import com.example.ridesafe.sensor.CompassSensorTracker
import com.example.ridesafe.sensor.EnvironmentLightTracker
import com.example.ridesafe.sensor.LightStatus
import com.example.ridesafe.sensor.MotionSensorTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RideState {
    IDLE,
    RECORDING,
    PAUSED
}

class RideViewModel(
    application: Application,
    private val repository: RideRepository
) : AndroidViewModel(application) {

    // --- SENSORS ---
    private var motionTracker: MotionSensorTracker? = null
    private var compassTracker: CompassSensorTracker? = null
    private var lightTracker: EnvironmentLightTracker? = null

    // Sensor Availability
    val isAccelerometerAvailable: Boolean
    val isCompassAvailable: Boolean
    val isLightSensorAvailable: Boolean

    // Live Sensor Values
    private val _motionValues = MutableStateFlow(Triple(0f, 0f, 9.81f))
    val motionValues: StateFlow<Triple<Float, Float, Float>> = _motionValues.asStateFlow()

    private val _motionMagnitude = MutableStateFlow(9.81f)
    val motionMagnitude: StateFlow<Float> = _motionMagnitude.asStateFlow()

    private val _compassDegrees = MutableStateFlow(0f)
    val compassDegrees: StateFlow<Float> = _compassDegrees.asStateFlow()

    private val _compassDirection = MutableStateFlow("Utara (N)")
    val compassDirection: StateFlow<String> = _compassDirection.asStateFlow()

    private val _lightStatus = MutableStateFlow(LightStatus())
    val lightStatus: StateFlow<LightStatus> = _lightStatus.asStateFlow()

    // Dark Mode Override: null = auto (by light sensor), true = force dark, false = force light
    private val _darkModeOverride = MutableStateFlow<Boolean?>(null)
    val darkModeOverride: StateFlow<Boolean?> = _darkModeOverride.asStateFlow()

    // --- RIDE RECORDING STATE ---
    private val _rideState = MutableStateFlow(RideState.IDLE)
    val rideState: StateFlow<RideState> = _rideState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _bumpCount = MutableStateFlow(0)
    val bumpCount: StateFlow<Int> = _bumpCount.asStateFlow()

    private val _harshBrakeCount = MutableStateFlow(0)
    val harshBrakeCount: StateFlow<Int> = _harshBrakeCount.asStateFlow()

    private val _liveSafetyScore = MutableStateFlow(100)
    val liveSafetyScore: StateFlow<Int> = _liveSafetyScore.asStateFlow()

    private val _activeTripEvents = MutableStateFlow<List<RideEvent>>(emptyList())
    val activeTripEvents: StateFlow<List<RideEvent>> = _activeTripEvents.asStateFlow()

    // Quick Alerts
    private val _bannerAlert = MutableStateFlow<String?>(null)
    val bannerAlert: StateFlow<String?> = _bannerAlert.asStateFlow()

    // --- DATABASE & HISTORY ---
    private val _allTrips = MutableStateFlow<List<TripSession>>(emptyList())
    val allTrips: StateFlow<List<TripSession>> = _allTrips.asStateFlow()

    private val _selectedTrip = MutableStateFlow<TripSession?>(null)
    val selectedTrip: StateFlow<TripSession?> = _selectedTrip.asStateFlow()

    private val _selectedTripEvents = MutableStateFlow<List<RideEvent>>(emptyList())
    val selectedTripEvents: StateFlow<List<RideEvent>> = _selectedTripEvents.asStateFlow()

    // Timer Job
    private var timerJob: Job? = null
    private var rideStartTime: Long = 0L

    init {
        val app = getApplication<Application>()

        motionTracker = MotionSensorTracker(
            context = app,
            onBumpDetected = { mag -> handleBumpDetected(mag) },
            onHarshBrakeDetected = { mag -> handleHarshBrakeDetected(mag) },
            onMotionValues = { x, y, z, mag ->
                _motionValues.value = Triple(x, y, z)
                _motionMagnitude.value = mag
            }
        )

        compassTracker = CompassSensorTracker(
            context = app,
            onHeadingChanged = { deg, dir ->
                _compassDegrees.value = deg
                _compassDirection.value = dir
            }
        )

        lightTracker = EnvironmentLightTracker(
            context = app,
            onLightChanged = { status ->
                _lightStatus.value = status
            }
        )

        isAccelerometerAvailable = motionTracker?.isAvailable == true
        isCompassAvailable = compassTracker?.isAvailable == true
        isLightSensorAvailable = lightTracker?.isAvailable == true

        startSensors()
        loadTripHistory()
    }

    private fun startSensors() {
        motionTracker?.start()
        compassTracker?.start()
        lightTracker?.start()
    }

    fun stopSensors() {
        motionTracker?.stop()
        compassTracker?.stop()
        lightTracker?.stop()
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
        timerJob?.cancel()
    }

    // --- RIDE CONTROL ---
    fun startRide() {
        if (_rideState.value == RideState.IDLE) {
            rideStartTime = System.currentTimeMillis()
            _elapsedSeconds.value = 0L
            _bumpCount.value = 0
            _harshBrakeCount.value = 0
            _liveSafetyScore.value = 100
            _activeTripEvents.value = emptyList()
            _rideState.value = RideState.RECORDING
            startTimer()
            triggerVibration(100)
            showBanner("Perjalanan Dimulai. Sensor Aktif Memantau!")
        }
    }

    fun pauseRide() {
        if (_rideState.value == RideState.RECORDING) {
            _rideState.value = RideState.PAUSED
            timerJob?.cancel()
            showBanner("Perjalanan Dijeda.")
        }
    }

    fun resumeRide() {
        if (_rideState.value == RideState.PAUSED) {
            _rideState.value = RideState.RECORDING
            startTimer()
            showBanner("Perjalanan Dilanjutkan.")
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_rideState.value == RideState.RECORDING) {
                delay(1000)
                _elapsedSeconds.value += 1
                calculateSafetyScore()
            }
        }
    }

    fun finishAndSaveRide(customTitle: String? = null, notes: String = "") {
        val endTime = System.currentTimeMillis()
        val duration = _elapsedSeconds.value
        val bumps = _bumpCount.value
        val brakes = _harshBrakeCount.value
        val score = _liveSafetyScore.value
        val rating = calculateRating(score)

        val defaultTitle = "Perjalanan #${_allTrips.value.size + 1}"
        val finalTitle = if (!customTitle.isNullOrBlank()) customTitle else defaultTitle

        val trip = TripSession(
            title = finalTitle,
            startTime = rideStartTime,
            endTime = endTime,
            durationSeconds = duration,
            bumpCount = bumps,
            harshBrakeCount = brakes,
            safetyScore = score,
            safetyRating = rating,
            notes = notes
        )

        viewModelScope.launch {
            repository.saveTripWithEvents(trip, _activeTripEvents.value)
            loadTripHistory()
            resetActiveRide()
            showBanner("Perjalanan '$finalTitle' Berhasil Disimpan ke SQLite!")
        }
    }

    fun discardRide() {
        resetActiveRide()
        showBanner("Sesi perjalanan dibatalkan.")
    }

    private fun resetActiveRide() {
        timerJob?.cancel()
        _rideState.value = RideState.IDLE
        _elapsedSeconds.value = 0L
        _bumpCount.value = 0
        _harshBrakeCount.value = 0
        _liveSafetyScore.value = 100
        _activeTripEvents.value = emptyList()
    }

    // --- SENSOR EVENT HANDLERS ---
    private fun handleBumpDetected(magnitude: Float) {
        if (_rideState.value != RideState.RECORDING) return

        _bumpCount.value += 1
        val severity = when {
            magnitude > 12.0f -> "TINGGI"
            magnitude > 8.0f -> "SEDANG"
            else -> "RINGAN"
        }

        val event = RideEvent(
            tripId = 0,
            timestamp = System.currentTimeMillis(),
            eventType = "POTHOLE_BUMP",
            severity = severity,
            sensorMagnitude = magnitude,
            description = "Guncangan jalan terdeteksi (+${String.format("%.1f", magnitude)} m/s²)"
        )
        _activeTripEvents.value = _activeTripEvents.value + event
        calculateSafetyScore()
        triggerVibration(150)
        showBanner("Guncangan / Jalan Berlubang Terdeteksi!")
    }

    private fun handleHarshBrakeDetected(magnitude: Float) {
        if (_rideState.value != RideState.RECORDING) return

        _harshBrakeCount.value += 1
        val event = RideEvent(
            tripId = 0,
            timestamp = System.currentTimeMillis(),
            eventType = "HARSH_BRAKE",
            severity = "SEDANG",
            sensorMagnitude = magnitude,
            description = "Pengereman mendadak terdeteksi (${String.format("%.1f", magnitude)} m/s²)"
        )
        _activeTripEvents.value = _activeTripEvents.value + event
        calculateSafetyScore()
        triggerVibration(250)
        showBanner("Peringatan: Pengereman Mendadak!")
    }

    private fun calculateSafetyScore() {
        // Algoritma Penilaian Keselamatan Berkendara (0 - 100)
        // Nilai dasar: 100
        // Penalti per guncangan: -3 poin
        // Penalti per pengereman mendadak: -6 poin
        val penalty = (_bumpCount.value * 3) + (_harshBrakeCount.value * 6)
        val finalScore = (100 - penalty).coerceIn(10, 100)
        _liveSafetyScore.value = finalScore
    }

    private fun calculateRating(score: Int): String {
        return when {
            score >= 90 -> "Sangat Aman"
            score >= 75 -> "Aman"
            score >= 60 -> "Cukup Berhati-hati"
            else -> "Perlu Perbaikan"
        }
    }

    // --- HISTORY OPERATIONS ---
    fun loadTripHistory() {
        viewModelScope.launch {
            _allTrips.value = repository.getAllTrips()
        }
    }

    fun selectTrip(trip: TripSession) {
        _selectedTrip.value = trip
        viewModelScope.launch {
            _selectedTripEvents.value = repository.getEventsForTrip(trip.id)
        }
    }

    fun clearSelectedTrip() {
        _selectedTrip.value = null
        _selectedTripEvents.value = emptyList()
    }

    fun deleteTrip(tripId: Long) {
        viewModelScope.launch {
            repository.deleteTrip(tripId)
            loadTripHistory()
            if (_selectedTrip.value?.id == tripId) {
                clearSelectedTrip()
            }
            showBanner("Catatan perjalanan dihapus dari SQLite.")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllData()
            loadTripHistory()
            clearSelectedTrip()
            showBanner("Seluruh riwayat perjalanan berhasil dibersihkan.")
        }
    }

    // --- SIMULATOR & TESTING (Untuk Uji Coba Tanpa Pergerakan Fisik / Emulator) ---
    fun simulateBump(mag: Float = 9.2f) {
        handleBumpDetected(mag)
    }

    fun simulateHarshBrake(mag: Float = 7.5f) {
        handleHarshBrakeDetected(mag)
    }

    fun simulateLightCondition(lux: Float) {
        val simulated = lightTracker?.classifyLight(lux) ?: LightStatus(lux = lux)
        _lightStatus.value = simulated
        showBanner("Simulasi Sensor Cahaya: ${simulated.condition} (${lux.toInt()} lx)")
    }

    fun setDarkModeOverride(override: Boolean?) {
        _darkModeOverride.value = override
    }

    // --- BANNER ALERT ---
    private fun showBanner(msg: String) {
        _bannerAlert.value = msg
        viewModelScope.launch {
            delay(3000)
            if (_bannerAlert.value == msg) {
                _bannerAlert.value = null
            }
        }
    }

    // --- VIBRATION HELPER ---
    private fun triggerVibration(durationMs: Long) {
        val app = getApplication<Application>()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
            // Ignored if device lacks vibrator
        }
    }
}
