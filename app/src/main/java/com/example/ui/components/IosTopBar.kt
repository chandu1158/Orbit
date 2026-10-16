package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIosColors

@Composable
fun IosTopBar(
    title: String,
    leadingAction: (@Composable () -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    contentBelowTitle: (@Composable () -> Unit)? = null,
    isLargeTitle: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LocalIosColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Top navigation bar row (small actions)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                leadingAction?.invoke()
            }

            if (!isLargeTitle) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                trailingAction?.invoke()
            }
        }

        // iOS Large Title
        if (isLargeTitle) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp),
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 2.dp, bottom = 6.dp)
                    .testTag("large_screen_title")
            )
        }

        contentBelowTitle?.invoke()
    }
}

@Composable
fun IosBackButton(
    label: String = "Back",
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIosColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(onClick = onBack)
            .padding(vertical = 8.dp)
            .testTag("back_button")
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = "Back",
            tint = colors.accentBlue,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.accentBlue
        )
    }
}
