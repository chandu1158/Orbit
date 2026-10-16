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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telecom.CallUiStatus
import com.example.telecom.CurrentCallUiState
import com.example.ui.theme.IosRed

@Composable
fun ActiveCallScreen(
    state: CurrentCallUiState,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleHold: () -> Unit,
    onPlayDtmf: (Char) -> Unit,
    onOpenContacts: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDtmfKeypad by remember { mutableStateOf(false) }

    val displayName = state.callerName ?: state.callerNumber.ifBlank { "Unknown" }

    val statusSubtitle = when (state.status) {
        CallUiStatus.DIALING -> "Calling…"
        CallUiStatus.CONNECTING -> "Connecting…"
        CallUiStatus.HOLDING -> "On Hold"
        CallUiStatus.DISCONNECTING -> "Disconnecting…"
        CallUiStatus.DISCONNECTED -> state.disconnectCause ?: "Call Ended"
        CallUiStatus.ACTIVE -> formatDuration(state.durationSeconds)
        else -> ""
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090C))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("active_call_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper Info: Caller Name, Phone, Duration
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (state.callerName != null && state.callerNumber.isNotBlank()) {
                    Text(
                        text = state.callerNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = statusSubtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = if (state.isHolding) Color(0xFFFF9500) else Color(0xFF8E8E93)
                )
            }

            // Middle: 6 iOS Circular Call Controls Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Row 1: Mute | Keypad | Speaker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InCallActionButton(
                        icon = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = "mute",
                        isActive = state.isMuted,
                        onClick = onToggleMute,
                        testTag = "incall_mute_button"
                    )

                    InCallActionButton(
                        icon = Icons.Default.Dialpad,
                        label = "keypad",
                        isActive = showDtmfKeypad,
                        onClick = { showDtmfKeypad = true },
                        testTag = "incall_keypad_button"
                    )

                    InCallActionButton(
                        icon = Icons.Default.VolumeUp,
                        label = "speaker",
                        isActive = state.isSpeaker,
                        onClick = onToggleSpeaker,
                        testTag = "incall_speaker_button"
                    )
                }

                // Row 2: Add Call | Hold | Contacts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InCallActionButton(
                        icon = Icons.Default.Add,
                        label = "add call",
                        isActive = false,
                        onClick = onOpenContacts,
                        testTag = "incall_add_call_button"
                    )

                    InCallActionButton(
                        icon = if (state.isHolding) Icons.Default.PlayArrow else Icons.Default.Pause,
                        label = if (state.isHolding) "resume" else "hold",
                        isActive = state.isHolding,
                        onClick = onToggleHold,
                        testTag = "incall_hold_button"
                    )

                    InCallActionButton(
                        icon = Icons.Default.Contacts,
                        label = "contacts",
                        isActive = false,
                        onClick = onOpenContacts,
                        testTag = "incall_contacts_button"
                    )
                }
            }

            // Bottom: Large Red End Call Button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(IosRed)
                    .clickable(onClick = onEndCall)
                    .testTag("incall_end_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        // Overlay Keypad if requested
        if (showDtmfKeypad) {
            InCallKeypadOverlay(
                onDigitPressed = onPlayDtmf,
                onDismiss = { showDtmfKeypad = false }
            )
        }
    }
}

@Composable
fun InCallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) Color.White else Color(0x33FFFFFF)
    val iconColor = if (isActive) Color.Black else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(bgColor)
                .clickable(onClick = onClick)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
