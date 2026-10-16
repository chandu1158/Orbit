package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallLogItem
import com.example.data.model.CallType
import com.example.ui.theme.LocalIosColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentCallRow(
    item: CallLogItem,
    onClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIosColors.current
    val isMissed = item.type == CallType.MISSED || item.type == CallType.REJECTED
    val primaryColor = if (isMissed) colors.callRed else colors.textPrimary

    val title = item.cachedName ?: item.formattedNumber.ifBlank { "Unknown" }
    val timeFormatted = formatTime(item.timestamp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("recent_call_row_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = primaryColor,
                        fontWeight = if (isMissed) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (item.callCount > 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${item.callCount})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    val subtitle = when (item.type) {
                        CallType.INCOMING -> "incoming"
                        CallType.OUTGOING -> "outgoing"
                        CallType.MISSED -> "missed"
                        CallType.REJECTED -> "rejected"
                        CallType.BLOCKED -> "blocked"
                        CallType.UNKNOWN -> "phone"
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(end = 4.dp)
            )

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("call_info_button_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Call details",
                    tint = colors.accentBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.separator,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneDay = 24 * 60 * 60 * 1000L

    return when {
        diff < oneDay -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
        diff < 7 * oneDay -> {
            SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        }
        else -> {
            SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
