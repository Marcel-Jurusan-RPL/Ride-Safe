package com.example.ridesafe.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ridesafe.ui.theme.DangerRed
import com.example.ridesafe.ui.theme.SafeGreen
import com.example.ridesafe.ui.theme.WarningAmber

@Composable
fun SafetyScoreBadge(
    score: Int,
    rating: String,
    bumpCount: Int,
    harshBrakeCount: Int,
    modifier: Modifier = Modifier
) {
    val scoreColor by animateColorAsState(
        targetValue = when {
            score >= 90 -> SafeGreen
            score >= 75 -> SafeGreen.copy(alpha = 0.85f)
            score >= 60 -> WarningAmber
            else -> DangerRed
        },
        label = "ScoreColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Big Circular Score
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(scoreColor.copy(alpha = 0.12f))
                .border(3.dp, scoreColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = scoreColor
                )
                Text(
                    text = "Skor",
                    style = MaterialTheme.typography.labelSmall,
                    color = scoreColor
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Center / Right: Safety Evaluation & Statistics
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Evaluasi Keselamatan",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = rating,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPill(
                    label = "Guncangan",
                    count = bumpCount,
                    accentColor = if (bumpCount > 0) WarningAmber else MaterialTheme.colorScheme.onSurface
                )
                StatPill(
                    label = "Rem Kasar",
                    count = harshBrakeCount,
                    accentColor = if (harshBrakeCount > 0) DangerRed else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StatPill(label: String, count: Int, accentColor: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = "$count kali",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}
