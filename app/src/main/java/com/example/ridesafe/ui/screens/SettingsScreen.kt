package com.example.ridesafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ridesafe.ui.theme.*
import com.example.ridesafe.viewmodel.RideViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RideViewModel,
    modifier: Modifier = Modifier
) {
    val isAccelAvailable = viewModel.isAccelerometerAvailable
    val isCompassAvailable = viewModel.isCompassAvailable
    val isLightAvailable = viewModel.isLightSensorAvailable

    val motionMag by viewModel.motionMagnitude.collectAsState()
    val compassDeg by viewModel.compassDegrees.collectAsState()
    val compassDir by viewModel.compassDirection.collectAsState()
    val lightStatus by viewModel.lightStatus.collectAsState()

    val darkModeOverride by viewModel.darkModeOverride.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan & Diagnostik Sensor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Sensor Diagnostics
            item {
                Text(
                    text = "STATUS 3 SENSOR ANDROID",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CockpitCyan
                )
            }

            item {
                SensorStatusCard(
                    title = "1. Motion Sensor (Akselerometer)",
                    sensorType = "Sensor.TYPE_ACCELEROMETER",
                    isAvailable = isAccelAvailable,
                    functionDescription = "Mendeteksi getaran, jalan berlubang, dan deselerasi rem mendadak.",
                    liveValue = "${String.format("%.2f", motionMag)} m/s²",
                    icon = Icons.Default.Vibration
                )
            }

            item {
                SensorStatusCard(
                    title = "2. Position Sensor (Kompas & Magnet)",
                    sensorType = "Sensor.TYPE_MAGNETIC_FIELD + ACCEL",
                    isAvailable = isCompassAvailable,
                    functionDescription = "Menentukan arah mata angin pengendara secara real-time.",
                    liveValue = "${compassDeg.toInt()}° ($compassDir)",
                    icon = Icons.Default.Explore
                )
            }

            item {
                SensorStatusCard(
                    title = "3. Environment Sensor (Sensor Cahaya)",
                    sensorType = "Sensor.TYPE_LIGHT",
                    isAvailable = isLightAvailable,
                    functionDescription = "Mendeteksi cahaya redup/terowongan untuk auto-switch tema gelap.",
                    liveValue = "${lightStatus.lux.toInt()} Lux (${lightStatus.condition})",
                    icon = Icons.Default.Lightbulb
                )
            }

            // Section 2: Theme Settings
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TEMA & TAMPILAN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CockpitCyan
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Mode Tampilan Aplikasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Radio option: Auto by light sensor
                        ThemeRadioRow(
                            selected = darkModeOverride == null,
                            title = "Otomatis Sensor Cahaya (Rekomendasi)",
                            subtitle = "Otomatis beralih ke Mode Gelap saat di terowongan atau malam hari (< 50 Lux)",
                            onClick = { viewModel.setDarkModeOverride(null) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )

                        // Radio option: Always Dark
                        ThemeRadioRow(
                            selected = darkModeOverride == true,
                            title = "Selalu Mode Gelap (Dark Mode)",
                            subtitle = "Tampilan kontras tinggi khas kokpit malam hari",
                            onClick = { viewModel.setDarkModeOverride(true) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )

                        // Radio option: Always Light
                        ThemeRadioRow(
                            selected = darkModeOverride == false,
                            title = "Selalu Mode Terang (Light Mode)",
                            subtitle = "Tampilan bersih untuk siang hari",
                            onClick = { viewModel.setDarkModeOverride(false) }
                        )
                    }
                }
            }

            // Section 3: App & Project Info
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "INFORMASI PROYEK",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CockpitCyan
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "RideSafe - Smart Cycling & Driving Logger",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CockpitCyan
                        )
                        Text(
                            text = "Asisten berkendara cerdas dengan logging SQLite, kompas real-time, deteksi guncangan jalan berlubang, dan adaptasi cahaya lingkungan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        InfoBullet(label = "Arsitektur", value = "MVVM (Model - View - ViewModel)")
                        InfoBullet(label = "Antarmuka", value = "100% Kotlin Jetpack Compose (Material 3)")
                        InfoBullet(label = "Database", value = "SQLite Database Engine (Room Pattern)")
                        InfoBullet(label = "Sensor Digunakan", value = "3 Sensor (Motion, Position, Environment)")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SensorStatusCard(
    title: String,
    sensorType: String,
    isAvailable: Boolean,
    functionDescription: String,
    liveValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CockpitCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = CockpitCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = sensorType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isAvailable) SafeGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isAvailable) "Tersedia" else "Simulasi",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isAvailable) SafeGreen else WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = functionDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Nilai Real-Time: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = liveValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = CockpitCyan
                )
            }
        }
    }
}

@Composable
private fun ThemeRadioRow(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) CockpitCyan.copy(alpha = 0.08f) else Color.Transparent)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = CockpitCyan)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun InfoBullet(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "• $label: ",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
