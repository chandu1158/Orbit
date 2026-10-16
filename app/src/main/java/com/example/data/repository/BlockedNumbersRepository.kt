package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.BlockedNumberEntity
import kotlinx.coroutines.flow.Flow

class BlockedNumbersRepository(private val db: AppDatabase) {
    val blockedNumbers: Flow<List<BlockedNumberEntity>> = db.blockedNumberDao().getAllBlocked()

    suspend fun blockNumber(phoneNumber: String, label: String? = null): Long {
        val normalized = phoneNumber.filter { it.isDigit() || it == '+' }
        return db.blockedNumberDao().insert(
            BlockedNumberEntity(
                phoneNumber = phoneNumber,
                normalizedNumber = normalized,
                label = label
            )
        )
    }

    suspend fun unblockNumber(phoneNumber: String) {
        val normalized = phoneNumber.filter { it.isDigit() || it == '+' }
        db.blockedNumberDao().deleteByNumber(normalized)
    }

    suspend fun isBlocked(phoneNumber: String): Boolean {
        val normalized = phoneNumber.filter { it.isDigit() || it == '+' }
        return db.blockedNumberDao().isBlocked(normalized)
    }
}
