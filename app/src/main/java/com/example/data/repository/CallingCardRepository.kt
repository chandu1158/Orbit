package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.db.CallingCardEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CallingCardRepository(private val db: AppDatabase) {

    companion object {
        const val MY_CARD_ID = "my_card"

        fun normalizeKey(raw: String): String {
            if (raw == MY_CARD_ID) return MY_CARD_ID
            val digits = raw.filter { it.isDigit() || it == '+' }
            return if (digits.length > 10) digits.takeLast(10) else digits
        }
    }

    fun getCallingCardFlow(idOrNumber: String): Flow<CallingCardEntity?> {
        val key = normalizeKey(idOrNumber)
        return db.callingCardDao().getCallingCardFlow(key)
    }

    suspend fun getCallingCard(idOrNumber: String): CallingCardEntity? = withContext(Dispatchers.IO) {
        val key = normalizeKey(idOrNumber)
        db.callingCardDao().getCallingCard(key)
    }

    fun getAllCallingCards(): Flow<List<CallingCardEntity>> {
        return db.callingCardDao().getAllCallingCards()
    }

    suspend fun saveCallingCard(card: CallingCardEntity) = withContext(Dispatchers.IO) {
        val key = normalizeKey(card.cardId)
        val normalizedCard = card.copy(
            cardId = key,
            updatedAt = System.currentTimeMillis()
        )
        db.callingCardDao().insertOrUpdate(normalizedCard)
    }

    suspend fun deleteCallingCard(context: Context, cardId: String) = withContext(Dispatchers.IO) {
        val key = normalizeKey(cardId)
        val card = db.callingCardDao().getCallingCard(key)
        card?.photoPath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                // Ignore failure to delete local cached file
            }
        }
        db.callingCardDao().delete(key)
    }

    suspend fun savePhotoFromUri(context: Context, cardId: String, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "calling_cards").apply { mkdirs() }
            val file = File(dir, "card_${normalizeKey(cardId)}_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    suspend fun savePhotoFromBitmap(context: Context, cardId: String, bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "calling_cards").apply { mkdirs() }
            val file = File(dir, "card_${normalizeKey(cardId)}_${System.currentTimeMillis()}.jpg")

            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun createDefaultCard(cardId: String, name: String, subtitle: String = ""): CallingCardEntity {
        return CallingCardEntity(
            cardId = normalizeKey(cardId),
            displayName = name.ifBlank { "Voxa User" },
            subtitle = subtitle,
            photoPath = null,
            gradientIndex = 0,
            fontStyleIndex = 0,
            textColorHex = "#FFFFFF",
            textPlacement = "TOP",
            photoScale = 1.0f,
            photoOffsetY = 0f,
            framingStyle = "FULL"
        )
    }
}
