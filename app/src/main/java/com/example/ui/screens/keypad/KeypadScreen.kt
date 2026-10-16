package com.example.ui.screens.keypad

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.data.model.SimAccountInfo
import com.example.ui.components.AvatarView
import com.example.ui.components.SimSelectionDialog
import com.example.ui.theme.LocalIosColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadScreen(
    viewModel: KeypadViewModel,
    onAddContactWithNumber: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalIosColors.current
    val view = LocalView.current
    var simPickerInfo by remember { mutableStateOf<Pair<List<SimAccountInfo>, String>?>(null) }

    simPickerInfo?.let { (sims, number) ->
        SimSelectionDialog(
            sims = sims,
            targetNumber = number,
            onSimSelected = { sim ->
                viewModel.callWithSim(sim, number)
                simPickerInfo = null
            },
            onDismiss = { simPickerInfo = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Area: Number Display & T9 Suggestions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // T9 Suggestions Bar
            AnimatedVisibility(visible = uiState.suggestedContacts.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("t9_suggestions_row"),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(uiState.suggestedContacts, key = { it.contactId }) { contact ->
                        Surface(
                            onClick = {
                                val number = contact.numbers.firstOrNull()?.number ?: ""
                                viewModel.setNumber(number)
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = colors.searchBg,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarView(name = contact.displayName, size = 24.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = contact.displayName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Number Display
            Text(
                text = uiState.enteredNumber.ifEmpty { " " },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (uiState.enteredNumber.length > 12) 28.sp else 36.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .testTag("keypad_display")
            )

            // Add Number Option
            if (uiState.enteredNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(onClick = { onAddContactWithNumber(uiState.enteredNumber) })
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("add_number_action"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Number",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.accentBlue
                    )
                }
            }
        }

        // Keypad Grid & Call Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KeypadDigitButton(
                    digit = "1",
                    subtext = "",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('1')
                    }
                )
                KeypadDigitButton(
                    digit = "2",
                    subtext = "ABC",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('2')
                    }
                )
                KeypadDigitButton(
                    digit = "3",
                    subtext = "DEF",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('3')
                    }
                )
            }

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KeypadDigitButton(
                    digit = "4",
                    subtext = "GHI",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('4')
                    }
                )
                KeypadDigitButton(
                    digit = "5",
                    subtext = "JKL",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('5')
                    }
                )
                KeypadDigitButton(
                    digit = "6",
                    subtext = "MNO",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('6')
                    }
                )
            }

            // Row 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KeypadDigitButton(
                    digit = "7",
                    subtext = "PQRS",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('7')
                    }
                )
                KeypadDigitButton(
                    digit = "8",
                    subtext = "TUV",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('8')
                    }
                )
                KeypadDigitButton(
                    digit = "9",
                    subtext = "WXYZ",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('9')
                    }
                )
            }

            // Row 4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KeypadDigitButton(
                    digit = "*",
                    subtext = "",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('*')
                    }
                )
                KeypadDigitButton(
                    digit = "0",
                    subtext = "+",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('0')
                    },
                    onLongClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        viewModel.appendPlus()
                    }
                )
                KeypadDigitButton(
                    digit = "#",
                    subtext = "",
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.appendDigit('#')
                    }
                )
            }

            // Bottom Action Row: SIM Selector / Call Button / Backspace
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left slot: SIM Picker if dual-SIM
                Box(
                    modifier = Modifier.size(75.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.availableSims.size > 1) {
                        IconButton(
                            onClick = {
                                simPickerInfo = Pair(uiState.availableSims, uiState.enteredNumber)
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("sim_toggle_button")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.SimCard,
                                    contentDescription = "Switch SIM",
                                    tint = colors.accentBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = uiState.selectedSim?.displayName ?: "SIM",
                                    fontSize = 9.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Center slot: Green Call Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(colors.callGreen)
                        .combinedClickable(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.onCallClicked { sims, num ->
                                    simPickerInfo = Pair(sims, num)
                                }
                            }
                        )
                        .testTag("keypad_call_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Place Call",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Right slot: Backspace
                Box(
                    modifier = Modifier.size(75.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.enteredNumber.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        viewModel.backspace()
                                    },
                                    onLongClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                        viewModel.clearNumber()
                                    }
                                )
                                .testTag("keypad_backspace_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Delete",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadDigitButton(
    digit: String,
    subtext: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalIosColors.current

    Box(
        modifier = modifier
            .size(75.dp)
            .clip(CircleShape)
            .background(colors.keypadButton)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("keypad_button_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = if (digit in listOf("*", "#")) 34.sp else 32.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = colors.textPrimary,
                lineHeight = 36.sp
            )
            if (subtext.isNotEmpty()) {
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = colors.textPrimary
                )
            }
        }
    }
}
