package com.example.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import androidx.core.content.ContextCompat
import com.example.data.model.CallLogItem
import com.example.data.model.CallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class CallLogRepository(private val context: Context) {

    fun hasCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasWriteCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCallLogs(): List<CallLogItem> {
        if (!hasCallLogPermission()) return emptyList()

        val logs = mutableListOf<CallLogItem>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        val sortOrder = "${CallLog.Calls.DATE} DESC"

        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(CallLog.Calls._ID)
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val number = (if (numberIndex >= 0) it.getString(numberIndex) else null) ?: ""
                val cachedName = if (nameIndex >= 0) it.getString(nameIndex) else null
                val typeInt = if (typeIndex >= 0) it.getInt(typeIndex) else -1
                val timestamp = if (dateIndex >= 0) it.getLong(dateIndex) else 0L
                val duration = if (durationIndex >= 0) it.getLong(durationIndex) else 0L

                val callType = when (typeInt) {
                    CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                    CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                    CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                    CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
                    CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                    else -> CallType.UNKNOWN
                }

                logs.add(
                    CallLogItem(
                        id = id,
                        number = number,
                        formattedNumber = number,
                        cachedName = if (cachedName.isNullOrBlank()) null else cachedName,
                        type = callType,
                        timestamp = timestamp,
                        durationSeconds = duration
                    )
                )
            }
        }

        return groupConsecutiveCalls(logs)
    }

    private fun groupConsecutiveCalls(rawLogs: List<CallLogItem>): List<CallLogItem> {
        if (rawLogs.isEmpty()) return emptyList()

        val grouped = mutableListOf<CallLogItem>()
        var current = rawLogs.first()
        var count = 1

        for (i in 1 until rawLogs.size) {
            val next = rawLogs[i]
            // Group if same number and same call type and occurred within the same hour
            val sameNumber = current.number == next.number
            val sameType = current.type == next.type
            val withinHour = (current.timestamp - next.timestamp) < 3600 * 1000L

            if (sameNumber && sameType && withinHour) {
                count++
            } else {
                grouped.add(current.copy(callCount = count))
                current = next
                count = 1
            }
        }
        grouped.add(current.copy(callCount = count))
        return grouped
    }

    fun getCallLogsFlow(): Flow<List<CallLogItem>> = flow {
        emit(getCallLogs())
    }.flowOn(Dispatchers.IO)

    suspend fun deleteCallLog(id: Long): Boolean = withContext(Dispatchers.IO) {
        if (!hasWriteCallLogPermission()) return@withContext false
        try {
            val deleted = context.contentResolver.delete(
                CallLog.Calls.CONTENT_URI,
                "${CallLog.Calls._ID} = ?",
                arrayOf(id.toString())
            )
            deleted > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearAllCallLogs(): Boolean = withContext(Dispatchers.IO) {
        if (!hasWriteCallLogPermission()) return@withContext false
        try {
            val deleted = context.contentResolver.delete(
                CallLog.Calls.CONTENT_URI,
                null,
                null
            )
            deleted >= 0
        } catch (e: Exception) {
            false
        }
    }
}
