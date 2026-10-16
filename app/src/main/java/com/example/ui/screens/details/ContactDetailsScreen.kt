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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppDatabase
import com.example.data.db.FavoriteEntity
import com.example.data.model.ContactItem
import com.example.data.repository.BlockedNumbersRepository
import com.example.data.repository.FavoritesRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.IosBackButton
import com.example.ui.components.IosTopBar
import com.example.ui.theme.LocalIosColors
import kotlinx.coroutines.launch

@Composable
fun ContactDetailsScreen(
    contact: ContactItem,
    onBack: () -> Unit,
    onCallNumber: (String) -> Unit,
    onOpenCallingCard: (cardId: String, name: String, subtitle: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIosColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val favRepo = remember { FavoritesRepository(db) }
    val blockRepo = remember { BlockedNumbersRepository(db) }

    val primaryNumber = contact.numbers.firstOrNull()?.number ?: ""
    var isFavorite by remember { mutableStateOf(false) }
    var isBlocked by remember { mutableStateOf(false) }

    val cardRepo = remember { com.example.data.repository.CallingCardRepository(db) }
    var callingCard by remember { mutableStateOf<com.example.data.db.CallingCardEntity?>(null) }
    var showCallingCardCustomizer by remember { mutableStateOf(false) }

    LaunchedEffect(contact) {
        if (primaryNumber.isNotEmpty()) {
            isFavorite = favRepo.isFavorite(primaryNumber)
            isBlocked = blockRepo.isBlocked(primaryNumber)
            callingCard = cardRepo.getCallingCard(primaryNumber)
        }
    }

    if (showCallingCardCustomizer) {
        com.example.ui.components.CallingCardCustomizerDialog(
            contactName = contact.displayName,
            phoneNumber = primaryNumber,
            initialCard = callingCard,
            onSave = { newCard ->
                scope.launch {
                    cardRepo.saveCallingCard(newCard)
                    callingCard = newCard
                    showCallingCardCustomizer = false
                    Toast.makeText(context, "Calling Card Updated", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showCallingCardCustomizer = false }
        )
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
                IosBackButton(label = "Contacts", onBack = onBack)
            },
            trailingAction = {
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.accentBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            val editIntent = Intent(Intent.ACTION_EDIT).apply {
                                data = Uri.withAppendedPath(
                                    ContactsContract.Contacts.CONTENT_URI,
                                    contact.contactId.toString()
                                )
                            }
                            context.startActivity(editIntent)
                        }
                        .padding(8.dp)
                        .testTag("edit_contact_button")
                )
            }
        )

        // Contact Avatar and Name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarView(
                name = contact.displayName,
                photoUri = contact.photoUri,
                size = 88.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 26.sp),
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Action Buttons Row (Call, Message, Share)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ContactActionButton(
                icon = Icons.Default.Call,
                label = "call",
                onClick = { if (primaryNumber.isNotEmpty()) onCallNumber(primaryNumber) }
            )
            ContactActionButton(
                icon = Icons.AutoMirrored.Filled.Message,
                label = "message",
                onClick = {
                    if (primaryNumber.isNotEmpty()) {
                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$primaryNumber"))
                        context.startActivity(smsIntent)
                    }
                }
            )
            ContactActionButton(
                icon = Icons.Default.Share,
                label = "share",
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${contact.displayName}: $primaryNumber")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Contact"))
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Contact Photo & Poster Preview Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
                .clickable { showCallingCardCustomizer = true }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("customize_calling_card_banner"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        com.example.data.db.CallingCardGradients.getBrush(callingCard?.gradientIndex ?: 0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.displayName.firstOrNull()?.uppercase() ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Contact Photo & Poster",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (callingCard != null && callingCard?.tagline?.isNotBlank() == true) {
                        callingCard?.tagline ?: "Customized"
                    } else {
                        "Customize Calling Card"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.accentBlue
                )
            }

            Text(
                text = "Edit",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.accentBlue,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Numbers Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
        ) {
            contact.numbers.forEachIndexed { index, cn ->
                if (index > 0) {
                    HorizontalDivider(thickness = 0.5.dp, color = colors.separator, modifier = Modifier.padding(start = 16.dp))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCallNumber(cn.number) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = cn.typeLabel.lowercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = cn.number,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.accentBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options Card (Favorites, Copy, Block)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.secondaryBackground)
        ) {
            // Add/Remove Favorites
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            if (isFavorite) {
                                favRepo.removeByPhoneNumber(primaryNumber)
                                isFavorite = false
                                Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                            } else {
                                favRepo.addFavorite(
                                    FavoriteEntity(
                                        contactId = contact.contactId,
                                        lookupKey = contact.lookupKey,
                                        displayName = contact.displayName,
                                        phoneNumber = primaryNumber,
                                        photoUri = contact.photoUri
                                    )
                                )
                                isFavorite = true
                                Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.accentBlue,
                    modifier = Modifier.weight(1f)
                )
                if (isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Starred",
                        tint = colors.accentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = colors.separator, modifier = Modifier.padding(start = 16.dp))

            // Copy Number
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Phone Number", primaryNumber))
                        Toast.makeText(context, "Number copied", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Copy Phone Number",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.accentBlue
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = colors.separator, modifier = Modifier.padding(start = 16.dp))

            // Block Caller
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            if (isBlocked) {
                                blockRepo.unblockNumber(primaryNumber)
                                isBlocked = false
                                Toast.makeText(context, "Caller Unblocked", Toast.LENGTH_SHORT).show()
                            } else {
                                blockRepo.blockNumber(primaryNumber, contact.displayName)
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
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ContactActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIosColors.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.secondaryBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = colors.accentBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.accentBlue
        )
    }
}
