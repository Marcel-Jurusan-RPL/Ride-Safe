package com.example.ridesafe.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridesafe.ui.theme.CockpitCyan
import com.example.ridesafe.ui.theme.DangerRed
import com.example.ridesafe.ui.theme.SafeGreen
import com.example.ridesafe.ui.theme.WarningAmber
import kotlin.math.abs

@Composable
fun GForceMeter(
    x: Float,
    y: Float,
    z: Float,
    magnitude: Float,
    modifier: Modifier = Modifier
) {
    // Normal gravity is ~9.81 m/s^2. Excess indicates shock/bump
    val excessG = abs(magnitude - 9.81f)
    val isShock = excessG > 5.5f

    val badgeColor by animateColorAsState(
        targetValue = when {
            excessG > 10.0f -> DangerRed
            excessG > 5.5f -> WarningAmber
            else -> SafeGreen
        },
        label = "GForceColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "G-Force & Akselerometer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isShock) "Guncangan Jalan Terdeteksi!" else "Kondisi Berjalan Stabil",
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${String.format("%.1f", magnitude)} m/s²",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress bar representing total G load
        val loadProgress = (magnitude / 25.0f).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { loadProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = badgeColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mini Axis breakdown (X: Miring/Belok, Y: Maju/Rem, Z: Vertikal)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AxisIndicator(label = "X (Miring)", value = x)
            AxisIndicator(label = "Y (Rem/Gas)", value = y)
            AxisIndicator(label = "Z (Vertikal)", value = z)
        }
    }
}

@Composable
private fun AxisIndicator(label: String, value: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = "${String.format("%+.1f", value)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
