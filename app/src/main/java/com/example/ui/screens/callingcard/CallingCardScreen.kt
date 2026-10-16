package com.example.ui.screens.callingcard

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.CallingCardDesignSystem
import com.example.ui.components.IosBackButton
import com.example.ui.components.IosTopBar
import com.example.ui.theme.LocalIosColors
import com.example.ui.theme.OrbitGreen

@Composable
fun CallingCardScreen(
    cardId: String,
    initialName: String,
    initialSubtitle: String = "",
    viewModel: CallingCardViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalIosColors.current

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cardId) {
        viewModel.loadCard(cardId, initialName, initialSubtitle)
    }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setPhotoFromUri(context, uri)
        }
    }

    // Camera Snapshot Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.setPhotoFromBitmap(context, bitmap)
        }
    }

    // Fullscreen Realistic Incoming Call Preview
    if (uiState.isRealisticPreviewOpen) {
        CallingCardRealisticPreview(
            state = uiState,
            onClosePreview = { viewModel.toggleRealisticPreview(false) }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
            .statusBarsPadding()
    ) {
        // Navigation Top Bar
        IosTopBar(
            title = "Calling Card",
            isLargeTitle = false,
            leadingAction = {
                IosBackButton(label = "Cancel", onBack = onBack)
            },
            trailingAction = {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = OrbitGreen
                    )
                } else {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.titleMedium,
                        color = OrbitGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                viewModel.saveCard {
                                    Toast.makeText(context, "Calling Card Saved", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            }
                            .padding(8.dp)
                            .testTag("save_card_top_button")
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Interactive Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0x338E8E93), RoundedCornerShape(24.dp))
                    .testTag("calling_card_live_preview_box")
            ) {
                CallingCardPosterCanvas(
                    displayName = uiState.displayName,
                    subtitle = uiState.subtitle,
                    photoPath = uiState.photoPath,
                    gradientIndex = uiState.gradientIndex,
                    fontStyleIndex = uiState.fontStyleIndex,
                    textColorHex = uiState.textColorHex,
                    textPlacement = uiState.textPlacement,
                    photoScale = uiState.photoScale,
                    photoOffsetY = uiState.photoOffsetY,
                    framingStyle = uiState.framingStyle,
                    modifier = Modifier.fillMaxSize()
                )

                // Fullscreen realistic preview overlay button
                Button(
                    onClick = { viewModel.toggleRealisticPreview(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x99000000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .testTag("open_realistic_preview_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Realistic Preview",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Incoming Call Preview", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: PHOTO & FRAMING
            CardSectionHeader("PROFILE PHOTO & FRAMING")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.secondaryBackground)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colors.searchBg,
                            contentColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_photo_gallery_button")
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = "Gallery", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery", fontSize = 13.sp)
                    }

                    FilledTonalButton(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colors.searchBg,
                            contentColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("take_photo_camera_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Camera", fontSize = 13.sp)
                    }
                }

                if (uiState.photoPath != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    TextButton(
                        onClick = { viewModel.removePhoto(context) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Remove Current Photo", color = colors.callRed, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Framing Style Chips
                    Text(
                        text = "Framing Shape",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CallingCardDesignSystem.framingOptions.forEach { (label, code) ->
                            val isSelected = uiState.framingStyle == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OrbitGreen else colors.searchBg)
                                    .clickable { viewModel.setFramingStyle(code) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.Black else colors.textPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scale & Position Sliders
                    Text(
                        text = "Photo Zoom (${String.format("%.1fx", uiState.photoScale)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Slider(
                        value = uiState.photoScale,
                        onValueChange = viewModel::setPhotoScale,
                        valueRange = 0.8f..2.2f,
                        colors = SliderDefaults.colors(
                            thumbColor = OrbitGreen,
                            activeTrackColor = OrbitGreen
                        )
                    )

                    Text(
                        text = "Vertical Position",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Slider(
                        value = uiState.photoOffsetY,
                        onValueChange = viewModel::setPhotoOffsetY,
                        valueRange = -100f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = OrbitGreen,
                            activeTrackColor = OrbitGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: NAME & SUBTITLE
            CardSectionHeader("CALLER DETAILS")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.secondaryBackground)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.displayName,
                    onValueChange = viewModel::updateName,
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g. Alex Morgan") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_display_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.subtitle,
                    onValueChange = viewModel::updateSubtitle,
                    label = { Text("Subtitle / Role (Optional)") },
                    placeholder = { Text("e.g. Work, Family, VIP") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_subtitle_input")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 3: TEXT PLACEMENT & TYPOGRAPHY
            CardSectionHeader("TEXT PLACEMENT & FONT")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.secondaryBackground)
                    .padding(16.dp)
            ) {
                // Text Placement
                Text("Placement", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CallingCardDesignSystem.placementOptions.forEach { (label, code) ->
                        val isSelected = uiState.textPlacement == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) OrbitGreen else colors.searchBg)
                                .clickable { viewModel.setTextPlacement(code) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.Black else colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Font Style
                Text("Typography", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CallingCardDesignSystem.fontStyles.forEachIndexed { index, (label, font) ->
                        val isSelected = uiState.fontStyleIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) OrbitGreen else colors.searchBg)
                                .clickable { viewModel.setFontStyle(index) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label.split(" ").first(),
                                fontFamily = font,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.Black else colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text Color Palette
                Text("Text Color", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CallingCardDesignSystem.colorOptions.forEach { (name, hex) ->
                        val colorVal = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = uiState.textColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorVal)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) OrbitGreen else Color(0x33FFFFFF),
                                    shape = CircleShape
                                )
                                .clickable { viewModel.setTextColor(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = name,
                                    tint = if (hex == "#FFFFFF") Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4: BACKGROUND PALETTE
            CardSectionHeader("POSTER BACKGROUND")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.secondaryBackground)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CallingCardDesignSystem.gradientPresets.forEachIndexed { index, (name, _) ->
                        val isSelected = uiState.gradientIndex == index
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(CallingCardDesignSystem.getBackgroundBrush(index))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) OrbitGreen else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.setGradient(index) }
                                .testTag("calling_card_gradient_$index"),
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MANAGEMENT ACTIONS: Reset, Share, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reset
                OutlinedButton(
                    onClick = viewModel::resetToDefaults,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset")
                }

                // Share
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Calling Card: ${uiState.displayName} • ${uiState.subtitle}\nCrafted with Voxa Calling Cards"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Calling Card"))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }

                // Delete
                if (uiState.isExistingCard) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.callRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Calling Card") },
            text = { Text("Are you sure you want to delete this custom Calling Card? The card design and stored photo will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteCard(context) {
                            Toast.makeText(context, "Calling Card Deleted", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                ) {
                    Text("Delete", color = colors.callRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CardSectionHeader(title: String) {
    val colors = LocalIosColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = colors.textSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, bottom = 6.dp)
    )
}
