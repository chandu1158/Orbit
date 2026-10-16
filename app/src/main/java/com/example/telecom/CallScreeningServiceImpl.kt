package com.example.telecom

import android.telecom.Call
import android.telecom.CallScreeningService
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CallScreeningServiceImpl : CallScreeningService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: ""
        val normalizedNumber = number.filter { it.isDigit() || it == '+' }

        // Safety guard: NEVER filter or screen emergency numbers
        if (DialSanitizer.isEmergencyNumber(number)) {
            respondToCall(
                callDetails,
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .build()
            )
            return
        }

        scope.launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val isBlocked = db.blockedNumberDao().isBlocked(normalizedNumber)

                val response = if (isBlocked) {
                    CallResponse.Builder()
                        .setDisallowCall(true)
                        .setRejectCall(true)
                        .setSkipCallLog(false)
                        .setSkipNotification(true)
                        .build()
                } else {
                    CallResponse.Builder()
                        .setDisallowCall(false)
                        .setRejectCall(false)
                        .setSkipCallLog(false)
                        .setSkipNotification(false)
                        .build()
                }
                respondToCall(callDetails, response)
            } catch (e: Exception) {
                // Fail-safe: allow call through if screening check encounters an issue
                respondToCall(
                    callDetails,
                    CallResponse.Builder()
                        .setDisallowCall(false)
                        .setRejectCall(false)
                        .build()
                )
            }
        }
    }
}
