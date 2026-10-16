package com.example.telecom

import android.telecom.CallAudioState

enum class CallUiStatus {
    IDLE,
    RINGING,       // Incoming
    DIALING,       // Outgoing not connected yet
    CONNECTING,
    ACTIVE,        // In call
    HOLDING,
    DISCONNECTING,
    DISCONNECTED
}

data class CurrentCallUiState(
    val status: CallUiStatus = CallUiStatus.IDLE,
    val callerName: String? = null,
    val callerNumber: String = "",
    val photoUri: String? = null,
    val durationSeconds: Long = 0L,
    val isMuted: Boolean = false,
    val audioRoute: Int = CallAudioState.ROUTE_EARPIECE,
    val isSpeaker: Boolean = false,
    val isBluetooth: Boolean = false,
    val isHolding: Boolean = false,
    val canHold: Boolean = true,
    val isSpamSuspect: Boolean = false,
    val isVerifiedCaller: Boolean = false,
    val disconnectCause: String? = null,
    val callingCard: com.example.data.db.CallingCardEntity? = null
)
