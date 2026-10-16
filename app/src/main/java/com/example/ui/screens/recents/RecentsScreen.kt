package com.example.ui.screens.recents

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallLogItem
import com.example.ui.components.IosSearchBar
import com.example.ui.components.IosSegmentedControl
import com.example.ui.components.RecentCallRow
import com.example.ui.theme.LocalIosColors

@Composable
fun RecentsScreen(
    viewModel: RecentsViewModel,
    onCallNumber: (String) -> Unit,
    onOpenCallDetails: (CallLogItem) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalIosColors.current
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadRecents()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // iOS Top Header with Centered Segmented Control
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear button when editing, or Edit button
                if (isEditing) {
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.callRed,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                viewModel.clearAllLogs()
                                isEditing = false
                            }
                            .testTag("recents_clear_button")
                    )
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }

                // Centered Segmented Control ("All" | "Missed")
                IosSegmentedControl(
                    items = listOf("All", "Missed"),
                    selectedIndex = if (uiState.filter == RecentsFilter.ALL) 0 else 1,
                    onItemSelected = { index ->
                        viewModel.setFilter(if (index == 0) RecentsFilter.ALL else RecentsFilter.MISSED)
                    },
                    modifier = Modifier.size(width = 160.dp, height = 32.dp)
                )

                Text(
                    text = if (isEditing) "Done" else "Edit",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.accentBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { isEditing = !isEditing }
                        .testTag("recents_edit_button")
                )
            }

            // iOS Large Title
            Text(
                text = "Recents",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp),
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 4.dp)
                    .testTag("recents_screen_title")
            )
        }

        // Search Bar
        IosSearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::setSearchQuery,
            placeholder = "Search Recents"
        )

        // Permission Denied State
        if (!uiState.hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneMissed,
                        contentDescription = "Permission needed",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Call History Permission Needed",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To view your real cellular call log and missed calls, allow call history access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("grant_call_log_permission_button")
                    ) {
                        Text("Grant Permission", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            return
        }

        // Loading Indicator
        if (uiState.isLoading && uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accentBlue)
            }
            return
        }

        // Empty State
        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.filter == RecentsFilter.MISSED) "No Missed Calls" else "No Recents",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your recent cellular calls will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
            return
        }

        // Calls List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("recents_list")
        ) {
            items(uiState.items, key = { it.id }) { item ->
                RecentCallRow(
                    item = item,
                    onClick = { onCallNumber(item.number) },
                    onInfoClick = { onOpenCallDetails(item) }
                )
            }
        }
    }
}
