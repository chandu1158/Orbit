package com.example.incall

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.keypad.KeypadDigitButton

@Composable
fun InCallKeypadOverlay(
    onDigitPressed: (Char) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dtmfHistory by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // DTMF Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = dtmfHistory.ifEmpty { " " },
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
            }

            // Dialpad grid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadDigitButton("1", "", { dtmfHistory += '1'; onDigitPressed('1') })
                    KeypadDigitButton("2", "ABC", { dtmfHistory += '2'; onDigitPressed('2') })
                    KeypadDigitButton("3", "DEF", { dtmfHistory += '3'; onDigitPressed('3') })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadDigitButton("4", "GHI", { dtmfHistory += '4'; onDigitPressed('4') })
                    KeypadDigitButton("5", "JKL", { dtmfHistory += '5'; onDigitPressed('5') })
                    KeypadDigitButton("6", "MNO", { dtmfHistory += '6'; onDigitPressed('6') })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadDigitButton("7", "PQRS", { dtmfHistory += '7'; onDigitPressed('7') })
                    KeypadDigitButton("8", "TUV", { dtmfHistory += '8'; onDigitPressed('8') })
                    KeypadDigitButton("9", "WXYZ", { dtmfHistory += '9'; onDigitPressed('9') })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadDigitButton("*", "", { dtmfHistory += '*'; onDigitPressed('*') })
                    KeypadDigitButton("0", "+", { dtmfHistory += '0'; onDigitPressed('0') })
                    KeypadDigitButton("#", "", { dtmfHistory += '#'; onDigitPressed('#') })
                }
            }

            // Hide / Done button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("dismiss_keypad_button")
            ) {
                Text(
                    text = "Hide",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
