package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.CallingCardEntity
import com.example.data.db.CallingCardGradients
import com.example.ui.theme.LocalIosColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallingCardCustomizerDialog(
    contactName: String,
    phoneNumber: String,
    initialCard: CallingCardEntity?,
    onSave: (CallingCardEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalIosColors.current
    var selectedGradient by remember { mutableIntStateOf(initialCard?.gradientIndex ?: 0) }
    var selectedFontStyle by remember { mutableIntStateOf(initialCard?.fontStyleIndex ?: 0) }
    var tagline by remember { mutableStateOf(initialCard?.tagline ?: "") }

    val fontFamilies = listOf(FontFamily.SansSerif, FontFamily.Serif, FontFamily.Monospace)
    val fontLabels = listOf("Modern", "Editorial", "Mono")

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.secondaryBackground)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("calling_card_customizer_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contact Poster",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Calling Card Poster Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CallingCardGradients.getBrush(selectedGradient))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AvatarView(
                        name = contactName,
                        size = 64.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = fontFamilies[selectedFontStyle],
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    if (tagline.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tagline,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xCCFFFFFF),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Gradient Palette Selection
            Text(
                text = "POSTER COLOR GRADIENT",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textSecondary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CallingCardGradients.presets.forEachIndexed { index, (name, _) ->
                    val isSelected = index == selectedGradient
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CallingCardGradients.getBrush(index))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) colors.accentBlue else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedGradient = index }
                            .testTag("gradient_chip_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = name,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Font Style Selection
            Text(
                text = "TYPOGRAPHY STYLE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textSecondary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fontLabels.forEachIndexed { index, label ->
                    val isSelected = index == selectedFontStyle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.accentBlue else colors.searchBg)
                            .clickable { selectedFontStyle = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = fontFamilies[index],
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isSelected) Color.White else colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline Input
            OutlinedTextField(
                value = tagline,
                onValueChange = { tagline = it },
                label = { Text("Custom Tagline / Role") },
                placeholder = { Text("e.g. Family, VIP, Colleague") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    onSave(
                        CallingCardEntity(
                            phoneNumberKey = phoneNumber,
                            contactName = contactName,
                            gradientIndex = selectedGradient,
                            fontStyleIndex = selectedFontStyle,
                            tagline = tagline.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_calling_card_button")
            ) {
                Text("Save Calling Card", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
