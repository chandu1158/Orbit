package com.example.ui.screens.callingcard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.db.CallingCardDesignSystem
import com.example.ui.components.AvatarView
import java.io.File
import kotlin.math.roundToInt

@Composable
fun CallingCardPosterCanvas(
    displayName: String,
    subtitle: String,
    photoPath: String?,
    gradientIndex: Int,
    fontStyleIndex: Int,
    textColorHex: String,
    textPlacement: String,
    photoScale: Float,
    photoOffsetY: Float,
    framingStyle: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backgroundBrush = CallingCardDesignSystem.getBackgroundBrush(gradientIndex)
    val chosenFont = CallingCardDesignSystem.fontStyles[fontStyleIndex.coerceIn(0, 3)].second
    val textColor = try {
        Color(android.graphics.Color.parseColor(textColorHex))
    } catch (e: Exception) {
        Color.White
    }

    val photoFile = photoPath?.let { File(it) }
    val hasValidPhoto = photoFile != null && photoFile.exists()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Photo Layer
        if (hasValidPhoto) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val photoModifier = when (framingStyle) {
                    "ROUNDED" -> Modifier
                        .fillMaxWidth(0.88f)
                        .height(340.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(28.dp))
                    "CIRCLE" -> Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0x4DFFFFFF), CircleShape)
                    else -> Modifier.fillMaxSize() // "FULL"
                }

                Box(
                    modifier = photoModifier
                        .offset { IntOffset(0, photoOffsetY.roundToInt()) }
                        .graphicsLayer(scaleX = photoScale, scaleY = photoScale)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Calling Card Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            // Default Monogram Avatar when no custom photo is chosen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AvatarView(
                    name = displayName.ifBlank { "V" },
                    size = 120.dp
                )
            }
        }

        // Text Overlay Layer based on Text Placement
        val textAlignment = when (textPlacement) {
            "CENTER" -> Alignment.Center
            "BOTTOM" -> Alignment.BottomCenter
            else -> Alignment.TopCenter // "TOP"
        }

        val textPadding = when (textPlacement) {
            "TOP" -> Modifier.padding(top = 48.dp, start = 20.dp, end = 20.dp)
            "BOTTOM" -> Modifier.padding(bottom = 120.dp, start = 20.dp, end = 20.dp)
            else -> Modifier.padding(horizontal = 20.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(textPadding),
            contentAlignment = textAlignment
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayName.ifBlank { "Voxa User" },
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = chosenFont,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = chosenFont,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        color = textColor.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
