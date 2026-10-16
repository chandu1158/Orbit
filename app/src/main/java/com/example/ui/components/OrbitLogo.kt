package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.OrbitGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OrbitLogo(
    size: Dp = 48.dp,
    accentColor: Color = OrbitGreen,
    ringColor: Color = OrbitGreen,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val ringRadius = this.size.width * 0.38f
            val strokeWidth = this.size.width * 0.065f
            val centerDotRadius = this.size.width * 0.14f
            val satelliteDotRadius = this.size.width * 0.085f

            // Center Planet
            drawCircle(
                color = accentColor,
                radius = centerDotRadius,
                center = center
            )

            // Orbit Ring
            drawCircle(
                color = ringColor,
                radius = ringRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Orbiting Satellite at 45 degrees (2 o'clock)
            val angleRad = Math.toRadians(45.0)
            val satX = center.x + ringRadius * cos(angleRad).toFloat()
            val satY = center.y - ringRadius * sin(angleRad).toFloat()
            drawCircle(
                color = accentColor,
                radius = satelliteDotRadius,
                center = Offset(satX, satY)
            )
        }
    }
}
