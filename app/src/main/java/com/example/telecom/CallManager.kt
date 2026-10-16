package com.example.telecom

import android.content.Context
import android.net.Uri
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import com.example.data.repository.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object CallManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var inCallService: InCallService? = null
    private var primaryCall: Call? = null
    private var durationJob: Job? = null
    private var callStartTime: Long = 0L

    private val _callState = MutableStateFlow(CurrentCallUiState())
    val callState: StateFlow<CurrentCallUiState> = _callState.asStateFlow()

    private var contactsRepository: ContactsRepository? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        contactsRepository = ContactsRepository(context)
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            updateCallState(call)
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            updateCallState(call)
        }
    }

    fun setInCallService(service: InCallService?) {
        this.inCallService = service
    }

    fun onCallAdded(call: Call) {
        primaryCall = call
        call.registerCallback(callCallback)
        extractCallerInfo(call)
        updateCallState(call)
    }

    fun onCallRemoved(call: Call) {
        if (primaryCall == call) {
            call.unregisterCallback(callCallback)
            stopDurationTicker()
            primaryCall = null
            _callState.update {
                it.copy(
                    status = CallUiStatus.DISCONNECTED,
                    disconnectCause = call.details?.disconnectCause?.description?.toString() ?: "Call Ended"
                )
            }
            // Auto-reset to IDLE after a short delay for smooth UI dismissal
            scope.launch {
                delay(1500)
                if (primaryCall == null) {
                    _callState.value = CurrentCallUiState()
                }
            }
        }
    }

    fun onCallAudioStateChanged(audioState: CallAudioState) {
        val isSpeaker = (audioState.route and CallAudioState.ROUTE_SPEAKER) != 0
        val isBt = (audioState.route and CallAudioState.ROUTE_BLUETOOTH) != 0
        _callState.update {
            it.copy(
                isMuted = audioState.isMuted,
                audioRoute = audioState.route,
                isSpeaker = isSpeaker,
                isBluetooth = isBt
            )
        }
    }

    private fun extractCallerInfo(call: Call) {
        val handle = call.details?.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""
        var callerName: String? = call.details?.callerDisplayName
        if (callerName.isNullOrBlank()) {
            callerName = null
        }

        val isVerified = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            call.details?.callerNumberVerificationStatus == android.telecom.Connection.VERIFICATION_STATUS_PASSED
        } else false

        _callState.update {
            it.copy(
                callerName = callerName,
                callerNumber = rawNumber,
                isVerifiedCaller = isVerified
            )
        }

        // Asynchronously query ContactsContract and CallingCardRepository
        if (rawNumber.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
                var resolvedName: String? = null
                contactsRepository?.findContactByNumber(rawNumber)?.let { contact ->
                    resolvedName = contact.displayName
                    _callState.update {
                        it.copy(
                            callerName = contact.displayName,
                            photoUri = contact.photoUri ?: contact.photoThumbnailUri
                        )
                    }
                }
                appContext?.let { ctx ->
                    val card = com.example.data.repository.CallingCardRepository(
                        com.example.data.db.AppDatabase.getInstance(ctx)
                    ).getCallingCard(rawNumber)
                    if (card != null) {
                        _callState.update { it.copy(callingCard = card) }
                    }
                }
            }
        }
    }

    private fun updateCallState(call: Call) {
        val status = when (call.state) {
            Call.STATE_RINGING -> CallUiStatus.RINGING
            Call.STATE_DIALING, Call.STATE_CONNECTING -> CallUiStatus.DIALING
            Call.STATE_ACTIVE -> {
                startDurationTicker()
                CallUiStatus.ACTIVE
            }
            Call.STATE_HOLDING -> CallUiStatus.HOLDING
            Call.STATE_DISCONNECTING -> CallUiStatus.DISCONNECTING
            Call.STATE_DISCONNECTED -> {
                stopDurationTicker()
                CallUiStatus.DISCONNECTED
            }
            else -> CallUiStatus.ACTIVE
        }

        val canHold = (call.details?.callCapabilities?.and(Call.Details.CAPABILITY_HOLD) ?: 0) != 0
        val isHolding = call.state == Call.STATE_HOLDING

        _callState.update {
            it.copy(
                status = status,
                isHolding = isHolding,
                canHold = canHold
            )
        }
    }

    private fun startDurationTicker() {
        if (durationJob?.isActive == true) return
        if (callStartTime == 0L) {
            val connectTime = primaryCall?.details?.connectTimeMillis ?: 0L
            callStartTime = if (connectTime > 0L) connectTime else System.currentTimeMillis()
        }
        durationJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val elapsed = (System.currentTimeMillis() - callStartTime) / 1000L
                _callState.update { it.copy(durationSeconds = maxOf(0L, elapsed)) }
                delay(1000)
            }
        }
    }

    private fun stopDurationTicker() {
        durationJob?.cancel()
        durationJob = null
        callStartTime = 0L
    }

    // Call Control Actions
    fun answerCall() {
        primaryCall?.let {
            it.answer(VideoProfile.STATE_AUDIO_ONLY)
            updateCallState(it)
        }
    }

    fun declineCall() {
        primaryCall?.let {
            if (it.state == Call.STATE_RINGING) {
                it.reject(false, null)
            } else {
                it.disconnect()
            }
        }
    }

    fun endCall() {
        primaryCall?.disconnect()
    }

    fun toggleMute() {
        val nextMute = !_callState.value.isMuted
        inCallService?.setMuted(nextMute)
        _callState.update { it.copy(isMuted = nextMute) }
    }

    fun toggleSpeaker() {
        val currentRoute = _callState.value.audioRoute
        val newRoute = if ((currentRoute and CallAudioState.ROUTE_SPEAKER) != 0) {
            CallAudioState.ROUTE_EARPIECE
        } else {
            CallAudioState.ROUTE_SPEAKER
        }
        inCallService?.setAudioRoute(newRoute)
    }

    fun toggleHold() {
        primaryCall?.let { call ->
            if (call.state == Call.STATE_HOLDING) {
                call.unhold()
            } else if (call.state == Call.STATE_ACTIVE) {
                call.hold()
            }
        }
    }

    fun playDtmf(digit: Char) {
        primaryCall?.let { call ->
            call.playDtmfTone(digit)
            scope.launch {
                delay(200)
                call.stopDtmfTone()
            }
        }
    }
}
