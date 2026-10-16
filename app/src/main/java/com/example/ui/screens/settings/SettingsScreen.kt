package com.example.ui.screens.settings

import android.os.Build
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.IosBackButton
import com.example.ui.components.IosTopBar
import com.example.ui.theme.LocalIosColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onRequestDefaultDialer: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalIosColors.current

    LaunchedEffect(Unit) {
        viewModel.refreshRoleStatus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
            .verticalScroll(rememberScrollState())
    ) {
        IosTopBar(
            title = "",
            isLargeTitle = false,
            leadingAction = {
                IosBackButton(label = "Back", onBack = onBack)
            }
        )

        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp),
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Section: Appearance
        SettingsSectionHeader(title = "APPEARANCE")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
        ) {
            ThemeOptionRow(
                title = "Match System",
                isSelected = uiState.themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
            )
            HorizontalDivider(thickness = 0.5.dp, color = colors.separator, modifier = Modifier.padding(start = 16.dp))
            ThemeOptionRow(
                title = "Light Mode",
                isSelected = uiState.themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
            )
            HorizontalDivider(thickness = 0.5.dp, color = colors.separator, modifier = Modifier.padding(start = 16.dp))
            ThemeOptionRow(
                title = "Dark Mode",
                isSelected = uiState.themeMode == ThemeMode.DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
            )
        }

        // Section: Calling & Default Role
        SettingsSectionHeader(title = "CELLULAR & ROLE")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Default Phone App",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (uiState.isDefaultDialer) "Enabled (Active InCallService)" else "Not Set as Default",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.isDefaultDialer) colors.callGreen else colors.callRed
                    )
                }

                if (!uiState.isDefaultDialer) {
                    FilledTonalButton(
                        onClick = onRequestDefaultDialer,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = colors.accentBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Enable", color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp)
                    }
                }
            }

            if (uiState.availableSims.isNotEmpty()) {
                HorizontalDivider(thickness = 0.5.dp, color = colors.separator, modifier = Modifier.padding(start = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cellular SIMs Detected",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${uiState.availableSims.size} Active",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // Section: Keypad Feedback
        SettingsSectionHeader(title = "KEYPAD & HAPTICS")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keypad Haptic Feedback",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary
                )
                Switch(
                    checked = uiState.hapticFeedbackEnabled,
                    onCheckedChange = viewModel::setHapticEnabled,
                    colors = SwitchDefaults.colors(checkedThumbColor = androidx.compose.ui.graphics.Color.White, checkedTrackColor = colors.callGreen)
                )
            }
        }

        // Section: Blocked Callers
        SettingsSectionHeader(title = "BLOCKED CALLERS")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
        ) {
            if (uiState.blockedNumbers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "No Blocked Callers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            } else {
                uiState.blockedNumbers.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(thickness = 0.5.dp, color = colors.separator, modifier = Modifier.padding(start = 16.dp))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.label ?: item.phoneNumber,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textPrimary
                            )
                            if (item.label != null) {
                                Text(
                                    text = item.phoneNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Text(
                            text = "Unblock",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.callRed,
                            modifier = Modifier
                                .clickable { viewModel.unblockNumber(item.phoneNumber) }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        // Section: About & Device Diagnostic
        SettingsSectionHeader(title = "ABOUT & DEVICE DIAGNOSTICS")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                com.example.ui.components.OrbitLogo(size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Orbit",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Default Cellular Phone",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Text(
                text = "Target Device: Redmi Note 15 5G",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Android OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Build Model: ${Build.MANUFACTURER} ${Build.MODEL}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version 1.0 • Native Android Telecom",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    val colors = LocalIosColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = colors.textSecondary,
        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
fun ThemeOptionRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalIosColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = colors.accentBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
