package com.example.ridesafe.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ridesafe.ui.theme.CockpitCyan
import com.example.ridesafe.ui.theme.DangerRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassWidget(
    degrees: Float,
    direction: String,
    modifier: Modifier = Modifier
) {
    val animatedDegrees by animateFloatAsState(
        targetValue = degrees,
        animationSpec = tween(durationMillis = 150),
        label = "CompassRotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2

                // Outer decorative ring
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.3f),
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )

                // Dial ticks
                for (angle in 0 until 360 step 30) {
                    val angleRad = Math.toRadians(angle.toDouble())
                    val isMajor = angle % 90 == 0
                    val tickLen = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                    val tickColor = if (isMajor) CockpitCyan else Color.Gray.copy(alpha = 0.5f)
                    val strokeW = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()

                    val startX = center.x + (radius - tickLen) * sin(angleRad).toFloat()
                    val startY = center.y - (radius - tickLen) * cos(angleRad).toFloat()
                    val endX = center.x + radius * sin(angleRad).toFloat()
                    val endY = center.y - radius * cos(angleRad).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW
                    )
                }

                // Rotating Compass Needle
                rotate(degrees = -animatedDegrees, pivot = center) {
                    val needleLength = radius * 0.72f
                    val needleWidth = 12.dp.toPx()

                    // North Needle (Red)
                    val northPath = Path().apply {
                        moveTo(center.x, center.y - needleLength)
                        lineTo(center.x - needleWidth / 2, center.y)
                        lineTo(center.x + needleWidth / 2, center.y)
                        close()
                    }
                    drawPath(northPath, color = DangerRed)

                    // South Needle (Gray/Cyan)
                    val southPath = Path().apply {
                        moveTo(center.x, center.y + needleLength)
                        lineTo(center.x - needleWidth / 2, center.y)
                        lineTo(center.x + needleWidth / 2, center.y)
                        close()
                    }
                    drawPath(southPath, color = Color.Gray.copy(alpha = 0.7f))

                    // Center pivot circle
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = center)
                    drawCircle(color = DangerRed, radius = 3.dp.toPx(), center = center)
                }
            }

            // Heading degrees overlay at center-bottom
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 42.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${degrees.toInt()}°",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = direction,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = CockpitCyan
        )
    }
}
