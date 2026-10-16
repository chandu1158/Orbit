package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.example.data.model.SimAccountInfo
import com.example.ui.theme.LocalIosColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimSelectionDialog(
    sims: List<SimAccountInfo>,
    targetNumber: String,
    onSimSelected: (SimAccountInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalIosColors.current

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.secondaryBackground)
                .padding(top = 16.dp, bottom = 8.dp)
                .testTag("sim_selection_dialog")
        ) {
            Text(
                text = "Call $targetNumber",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Text(
                text = "Select SIM account to place this cellular call",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            sims.forEachIndexed { index, sim ->
                HorizontalDivider(thickness = 0.5.dp, color = colors.separator)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSimSelected(sim) }
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .testTag("sim_option_$index"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SimCard,
                        contentDescription = "SIM",
                        tint = colors.accentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = sim.displayName.ifBlank { "SIM ${index + 1}" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        if (!sim.carrierName.isNullOrBlank() && sim.carrierName != sim.displayName) {
                            Text(
                                text = sim.carrierName,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = colors.separator)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.callRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
