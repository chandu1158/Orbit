package com.example.data.db

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calling_cards")
data class CallingCardEntity(
    @PrimaryKey
    val cardId: String, // "my_card" for owner, or normalized phone number for contacts
    val displayName: String,
    val subtitle: String = "",
    val photoPath: String? = null,
    val gradientIndex: Int = 0,
    val fontStyleIndex: Int = 0, // 0: Modern, 1: Editorial Serif, 2: Mono, 3: Rounded
    val textColorHex: String = "#FFFFFF",
    val textPlacement: String = "TOP", // "TOP", "CENTER", "BOTTOM"
    val photoScale: Float = 1.0f,
    val photoOffsetY: Float = 0f,
    val framingStyle: String = "FULL", // "FULL", "ROUNDED", "CIRCLE"
    val updatedAt: Long = System.currentTimeMillis()
)

object CallingCardDesignSystem {
    // Voxa & iOS inspired background palettes
    val gradientPresets = listOf(
        // 0: Voxa Mint Space
        Pair("Voxa Emerald", listOf(Color(0xFF0B1F17), Color(0xFF133E2B), Color(0xFF0B0B0F))),
        // 1: Deep Twilight
        Pair("Deep Twilight", listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))),
        // 2: Sunset Coral
        Pair("Sunset Coral", listOf(Color(0xFF5A1E1E), Color(0xFF8B2635), Color(0xFF1E0E14))),
        // 3: Royal Violet
        Pair("Royal Amethyst", listOf(Color(0xFF231638), Color(0xFF452B6E), Color(0xFF130B22))),
        // 4: Rose Copper
        Pair("Rose Obsidian", listOf(Color(0xFF381F28), Color(0xFF633244), Color(0xFF120A0E))),
        // 5: Midnight Stealth (Near black minimalist)
        Pair("Midnight Stealth", listOf(Color(0xFF0B0B0F), Color(0xFF171720), Color(0xFF0B0B0F)))
    )

    fun getBackgroundBrush(index: Int): Brush {
        val safeIndex = index.coerceIn(0, gradientPresets.size - 1)
        return Brush.verticalGradient(gradientPresets[safeIndex].second)
    }

    val fontStyles = listOf(
        Pair("Modern Sans", FontFamily.SansSerif),
        Pair("Editorial Serif", FontFamily.Serif),
        Pair("Technical Mono", FontFamily.Monospace),
        Pair("System Default", FontFamily.Default)
    )

    val colorOptions = listOf(
        Pair("Pure White", "#FFFFFF"),
        Pair("Voxa Mint", "#34D399"),
        Pair("Sunset Gold", "#FCD34D"),
        Pair("Ice Blue", "#E0F2FE"),
        Pair("Rose Petal", "#FDA4AF"),
        Pair("Graphite", "#9CA3AF")
    )

    val placementOptions = listOf(
        Pair("Top Header", "TOP"),
        Pair("Hero Center", "CENTER"),
        Pair("Classic Bottom", "BOTTOM")
    )

    val framingOptions = listOf(
        Pair("Full Bleed", "FULL"),
        Pair("Rounded Card", "ROUNDED"),
        Pair("Circular Portrait", "CIRCLE")
    )
}
