package com.example.ui.screens.favorites

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.FavoriteEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.IosTopBar
import com.example.ui.theme.LocalIosColors

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onCallNumber: (String) -> Unit,
    onAddFavoriteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    val colors = LocalIosColors.current
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        IosTopBar(
            title = "Favorites",
            leadingAction = {
                IconButton(
                    onClick = onAddFavoriteClicked,
                    modifier = Modifier.testTag("add_favorite_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Favorite",
                        tint = colors.accentBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            trailingAction = {
                if (favorites.isNotEmpty()) {
                    Text(
                        text = if (isEditing) "Done" else "Edit",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.accentBlue,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { isEditing = !isEditing }
                            .padding(8.dp)
                            .testTag("edit_favorites_button")
                    )
                }
            }
        )

        if (favorites.isEmpty()) {
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
                        imageVector = Icons.Default.Star,
                        contentDescription = "No Favorites",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Favorites",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add contacts to favorites for instant speed dial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onAddFavoriteClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_favorite_empty_action")
                    ) {
                        Text("Add Favorite", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("favorites_list")
            ) {
                items(favorites, key = { it.id }) { favorite ->
                    FavoriteItemRow(
                        item = favorite,
                        isEditing = isEditing,
                        onClick = { onCallNumber(favorite.phoneNumber) },
                        onDelete = { viewModel.removeFavorite(favorite) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteItemRow(
    item: FavoriteEntity,
    isEditing: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIosColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("favorite_row_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditing) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 8.dp)
                        .testTag("delete_favorite_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.callRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            AvatarView(
                name = item.displayName,
                photoUri = item.photoUri,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.separator,
            modifier = Modifier.padding(start = 68.dp)
        )
    }
}
