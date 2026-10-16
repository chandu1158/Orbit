package com.example.ui.screens.contacts

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.ui.components.AvatarView
import com.example.ui.components.IosSearchBar
import com.example.ui.components.IosTopBar
import com.example.ui.theme.LocalIosColors

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    onContactClicked: (ContactItem) -> Unit,
    onAddContactClicked: () -> Unit,
    onOpenMyCard: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalIosColors.current

    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        IosTopBar(
            title = "Contacts",
            trailingAction = {
                IconButton(
                    onClick = onAddContactClicked,
                    modifier = Modifier.testTag("add_contact_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Contact",
                        tint = colors.accentBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        )

        IosSearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::setSearchQuery,
            placeholder = "Search Contacts"
        )

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
                        imageVector = Icons.Default.Contacts,
                        contentDescription = "Contacts Permission",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Contacts Access Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To search, browse, and call your contacts, grant contacts permission.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("grant_contacts_permission_button")
                    ) {
                        Text("Grant Permission", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            return
        }

        if (uiState.isLoading && uiState.contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accentBlue)
            }
            return
        }

        if (uiState.contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Contacts Found",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap + at the top right to create a new contact.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("contacts_list")
        ) {
            item(key = "my_calling_card_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenMyCard)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("my_calling_card_row")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarView(name = "My Card", size = 48.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "My Card",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Customize your Calling Card & Poster",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.accentBlue
                            )
                        }
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = colors.separator,
                        modifier = Modifier.padding(top = 12.dp, start = 62.dp)
                    )
                }
            }

            uiState.groupedContacts.forEach { (section, contactsInSection) ->
                item(key = "section_$section") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.secondaryBackground)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = section,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textSecondary
                        )
                    }
                }

                items(contactsInSection, key = { it.contactId }) { contact ->
                    ContactRowItem(
                        contact = contact,
                        onClick = { onContactClicked(contact) }
                    )
                }
            }
        }
    }
}

@Composable
fun ContactRowItem(
    contact: ContactItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIosColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("contact_item_${contact.contactId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarView(
                name = contact.displayName,
                photoUri = contact.photoThumbnailUri ?: contact.photoUri,
                size = 38.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                if (contact.numbers.isNotEmpty()) {
                    Text(
                        text = contact.numbers.first().number,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.separator,
            modifier = Modifier.padding(start = 66.dp)
        )
    }
}
