package com.example.ridesafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridesafe.sensor.LightStatus
import com.example.ridesafe.ui.components.*
import com.example.ridesafe.ui.theme.*
import com.example.ridesafe.viewmodel.RideState
import com.example.ridesafe.viewmodel.RideViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: RideViewModel,
    modifier: Modifier = Modifier
) {
    val rideState by viewModel.rideState.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val bumpCount by viewModel.bumpCount.collectAsState()
    val harshBrakeCount by viewModel.harshBrakeCount.collectAsState()
    val safetyScore by viewModel.liveSafetyScore.collectAsState()

    val motionValues by viewModel.motionValues.collectAsState()
    val motionMag by viewModel.motionMagnitude.collectAsState()
    val compassDeg by viewModel.compassDegrees.collectAsState()
    val compassDir by viewModel.compassDirection.collectAsState()
    val lightStatus by viewModel.lightStatus.collectAsState()
    val bannerAlert by viewModel.bannerAlert.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var tripTitleInput by remember { mutableStateOf("") }
    var tripNotesInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = CockpitCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RideSafe",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Recording Status Badge
                    val statusText = when (rideState) {
                        RideState.IDLE -> "Standby"
                        RideState.RECORDING -> "Merekam"
                        RideState.PAUSED -> "Dijeda"
                    }
                    val statusBg = when (rideState) {
                        RideState.IDLE -> MaterialTheme.colorScheme.surfaceVariant
                        RideState.RECORDING -> DangerRed
                        RideState.PAUSED -> WarningAmber
                    }

                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Temporary Alert Banner
            item {
                AnimatedVisibility(
                    visible = bannerAlert != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    bannerAlert?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = DangerRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DangerRed
                                )
                            }
                        }
                    }
                }
            }

            // HUD Top: Compass & Heading
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "KOMPAS DIGITAL REAL-TIME",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CompassWidget(
                            degrees = compassDeg,
                            direction = compassDir
                        )
                    }
                }
            }

            // Real-Time Ride Status HUD (When Recording / Paused)
            if (rideState != RideState.IDLE) {
                item {
                    val durationMin = elapsedSeconds / 60
                    val durationSec = elapsedSeconds % 60
                    val timeFormatted = String.format("%02d:%02d", durationMin, durationSec)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CockpitCyan.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Waktu Berkendara",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = timeFormatted,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CockpitCyan
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Status Rekaman",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = if (rideState == RideState.RECORDING) "AKTIF" else "PAUSE",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rideState == RideState.RECORDING) SafeGreen else WarningAmber
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            SafetyScoreBadge(
                                score = safetyScore,
                                rating = when {
                                    safetyScore >= 90 -> "Sangat Aman"
                                    safetyScore >= 75 -> "Aman"
                                    safetyScore >= 60 -> "Cukup Berhati-hati"
                                    else -> "Perlu Perbaikan"
                                },
                                bumpCount = bumpCount,
                                harshBrakeCount = harshBrakeCount
                            )
                        }
                    }
                }
            }

            // Motion Sensor (G-Force & Accelerometer)
            item {
                GForceMeter(
                    x = motionValues.first,
                    y = motionValues.second,
                    z = motionValues.third,
                    magnitude = motionMag
                )
            }

            // Environment Sensor (Light & Tunnel detection)
            item {
                LightConditionCard(lightStatus = lightStatus)
            }

            // Ride Action Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "KONTROL PERJALANAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        when (rideState) {
                            RideState.IDLE -> {
                                Button(
                                    onClick = { viewModel.startRide() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CockpitCyan),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = DarkBackground
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mulai Rekam Perjalanan",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                }
                            }
                            RideState.RECORDING -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.pauseRide() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Jeda")
                                    }

                                    Button(
                                        onClick = { showSaveDialog = true },
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Stop, contentDescription = null, tint = DarkBackground)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Selesai & Simpan", color = DarkBackground, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            RideState.PAUSED -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.resumeRide() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CockpitCyan),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DarkBackground)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Lanjutkan", color = DarkBackground)
                                    }

                                    Button(
                                        onClick = { showSaveDialog = true },
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, tint = DarkBackground)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Simpan Sesi", color = DarkBackground, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Sensor Simulation Panel (for Emulator / Demo presentation)
            item {
                SimulationControls(
                    onSimulateBump = { viewModel.simulateBump() },
                    onSimulateHarshBrake = { viewModel.simulateHarshBrake() },
                    onSimulateLight = { lux -> viewModel.simulateLightCondition(lux) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Save Trip Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Simpan Perjalanan ke SQLite",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ringkasan: ${bumpCount} guncangan, ${harshBrakeCount} rem mendadak. Skor: ${safetyScore}/100",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = tripTitleInput,
                        onValueChange = { tripTitleInput = it },
                        label = { Text("Nama Perjalanan (Opsional)") },
                        placeholder = { Text("Contoh: Perjalanan Kampus") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tripNotesInput,
                        onValueChange = { tripNotesInput = it },
                        label = { Text("Catatan Rute / Evaluasi") },
                        placeholder = { Text("Kondisi aspal berlubang di Jl. Sudirman") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishAndSaveRide(tripTitleInput, tripNotesInput)
                        showSaveDialog = false
                        tripTitleInput = ""
                        tripNotesInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                ) {
                    Text("Simpan", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.discardRide()
                    showSaveDialog = false
                    tripTitleInput = ""
                    tripNotesInput = ""
                }) {
                    Text("Buang Sesi", color = DangerRed)
                }
            }
        )
    }
}
