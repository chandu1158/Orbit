package com.example.ui.screens.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppDatabase
import com.example.data.model.CallLogItem
import com.example.data.model.CallType
import com.example.data.repository.BlockedNumbersRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.IosBackButton
import com.example.ui.components.IosTopBar
import com.example.ui.theme.LocalIosColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallDetailsScreen(
    item: CallLogItem,
    onBack: () -> Unit,
    onCallNumber: (String) -> Unit,
    onDeleteCall: (CallLogItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIosColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val blockRepo = remember { BlockedNumbersRepository(db) }

    var isBlocked by remember { mutableStateOf(false) }

    LaunchedEffect(item) {
        isBlocked = blockRepo.isBlocked(item.number)
    }

    val title = item.cachedName ?: item.formattedNumber.ifBlank { "Unknown" }
    val dateFormatted = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(item.timestamp))
    val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(item.timestamp))

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
                IosBackButton(label = "Recents", onBack = onBack)
            }
        )

        // Caller Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarView(
                name = title,
                size = 80.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                color = if (item.type == CallType.MISSED) colors.callRed else colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )

            if (item.cachedName != null && item.number.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.number,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }

        // Action Buttons Row (Call, Message)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ContactActionButton(
                icon = Icons.Default.Call,
                label = "call",
                onClick = { onCallNumber(item.number) }
            )
            ContactActionButton(
                icon = Icons.AutoMirrored.Filled.Message,
                label = "message",
                onClick = {
                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${item.number}"))
                    context.startActivity(smsIntent)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Call Info Card (Date, Time, Type, Duration)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
                .padding(16.dp)
        ) {
            Text(
                text = dateFormatted,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                    val typeText = when (item.type) {
                        CallType.INCOMING -> "Incoming Call"
                        CallType.OUTGOING -> "Outgoing Call"
                        CallType.MISSED -> "Missed Call"
                        CallType.REJECTED -> "Declined Call"
                        CallType.BLOCKED -> "Blocked Call"
                        CallType.UNKNOWN -> "Cellular Call"
                    }
                    Text(
                        text = typeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.type == CallType.MISSED) colors.callRed else colors.textSecondary
                    )
                }

                if (item.durationSeconds > 0) {
                    val minutes = item.durationSeconds / 60
                    val seconds = item.durationSeconds % 60
                    val durationStr = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
                    Text(
                        text = durationStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
        ) {
            if (item.cachedName == null) {
                // Create New Contact
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                                putExtra(ContactsContract.Intents.Insert.PHONE, item.number)
                            }
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Create New Contact",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.accentBlue
                    )
                }

                HorizontalDivider(thickness = 0.5.dp, color = colors.separator, modifier = Modifier.padding(start = 16.dp))
            }

            // Copy Number
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Number", item.number))
                        Toast.makeText(context, "Number copied", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Copy Number",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.accentBlue
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = colors.separator, modifier = Modifier.padding(start = 16.dp))

            // Block / Unblock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            if (isBlocked) {
                                blockRepo.unblockNumber(item.number)
                                isBlocked = false
                                Toast.makeText(context, "Caller Unblocked", Toast.LENGTH_SHORT).show()
                            } else {
                                blockRepo.blockNumber(item.number, item.cachedName)
                                isBlocked = true
                                Toast.makeText(context, "Caller Blocked", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = if (isBlocked) "Unblock this Caller" else "Block this Caller",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.callRed
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = colors.separator, modifier = Modifier.padding(start = 16.dp))

            // Delete From Recents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDeleteCall(item)
                        onBack()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Delete Call from Recents",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.callRed
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
