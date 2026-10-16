package com.example.incall

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.MainActivity
import com.example.telecom.CallManager
import com.example.telecom.CallUiStatus
import com.example.ui.theme.MyApplicationTheme

class InCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureWindowForLockscreen()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val callState by CallManager.callState.collectAsState()

                when (callState.status) {
                    CallUiStatus.RINGING -> {
                        IncomingCallScreen(
                            state = callState,
                            onAnswer = { CallManager.answerCall() },
                            onDecline = { CallManager.declineCall() },
                            onSendMessage = {
                                val number = callState.callerNumber
                                if (number.isNotBlank()) {
                                    CallManager.declineCall()
                                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number")).apply {
                                        putExtra("sms_body", "Can't talk right now. What's up?")
                                    }
                                    startActivity(smsIntent)
                                }
                            }
                        )
                    }

                    CallUiStatus.IDLE -> {
                        // Finish immediately if no active call
                        finish()
                    }

                    else -> {
                        // DIALING, ACTIVE, HOLDING, DISCONNECTING, DISCONNECTED
                        ActiveCallScreen(
                            state = callState,
                            onEndCall = {
                                CallManager.endCall()
                            },
                            onToggleMute = { CallManager.toggleMute() },
                            onToggleSpeaker = { CallManager.toggleSpeaker() },
                            onToggleHold = { CallManager.toggleHold() },
                            onPlayDtmf = { digit -> CallManager.playDtmf(digit) },
                            onOpenContacts = {
                                val km = getSystemService(android.app.KeyguardManager::class.java)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && km?.isKeyguardLocked == true) {
                                    km.requestDismissKeyguard(this@InCallActivity, object : android.app.KeyguardManager.KeyguardDismissCallback() {
                                        override fun onDismissSucceeded() {
                                            super.onDismissSucceeded()
                                            val intent = Intent(this@InCallActivity, MainActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                            }
                                            startActivity(intent)
                                        }
                                    })
                                } else {
                                    val intent = Intent(this@InCallActivity, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    }
                                    startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun configureWindowForLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
