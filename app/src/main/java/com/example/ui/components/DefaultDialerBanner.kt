package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIosColors

@Composable
fun DefaultDialerBanner(
    isDefaultDialer: Boolean,
    onSetDefaultClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isDefaultDialer) return

    val colors = LocalIosColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.searchBg)
            .padding(12.dp)
            .testTag("default_dialer_banner"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PhoneInTalk,
            contentDescription = "Default phone app",
            tint = colors.accentBlue,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Default Phone App",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary
            )
            Text(
                text = "Set as default for incoming call screens & dialer",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = colors.textSecondary
            )
        }

        FilledTonalButton(
            onClick = onSetDefaultClicked,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = colors.accentBlue,
                contentColor = androidx.compose.ui.graphics.Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("set_default_button")
        ) {
            Text("Set", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
