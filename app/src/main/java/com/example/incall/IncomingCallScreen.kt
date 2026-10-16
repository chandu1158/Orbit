package com.example.incall

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telecom.CurrentCallUiState
import com.example.ui.components.AvatarView
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosRed

@Composable
fun IncomingCallScreen(
    state: CurrentCallUiState,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = card?.displayName ?: (state.callerName ?: state.callerNumber.ifBlank { "Unknown" })
    val subtitleText = if (!card?.subtitle.isNullOrBlank()) {
        card.subtitle
    } else if (state.callerName != null) {
        state.callerNumber
    } else {
        "mobile • Voxa"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07070A))
            .testTag("incoming_call_screen")
    ) {
        if (card != null) {
            com.example.ui.screens.callingcard.CallingCardPosterCanvas(
                displayName = card.displayName,
                subtitle = card.subtitle,
                photoPath = card.photoPath,
                gradientIndex = card.gradientIndex,
                fontStyleIndex = card.fontStyleIndex,
                textColorHex = card.textColorHex,
                textPlacement = card.textPlacement,
                photoScale = card.photoScale,
                photoOffsetY = card.photoOffsetY,
                framingStyle = card.framingStyle,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper Info if no calling card was applied
            if (card == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    AvatarView(
                        name = displayName,
                        photoUri = state.photoUri,
                        size = 96.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFE5E5EA)
                    )

                    if (state.isSpamSuspect) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Suspected Spam",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = IosRed
                        )
                    }
                }
            } else {
                // If calling card is active, render a top spacer to maintain layout balance
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Lower Call Actions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Secondary Action: Message
                Row(
                    modifier = Modifier
                        .padding(bottom = 44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onSendMessage)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "Message",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Message",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }

                // Accept & Decline Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decline Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                                .background(IosRed)
                                .clickable(onClick = onDecline)
                                .testTag("incoming_decline_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "Decline",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Decline",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }

                    // Accept Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                                .background(IosGreen)
                                .clickable(onClick = onAnswer)
                                .testTag("incoming_accept_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Accept",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Accept",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
