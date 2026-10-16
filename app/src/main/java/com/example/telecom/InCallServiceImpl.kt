package com.example.telecom

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import com.example.incall.InCallActivity

class InCallServiceImpl : InCallService() {

    override fun onCreate() {
        super.onCreate()
        CallManager.init(applicationContext)
        CallManager.setInCallService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        CallManager.setInCallService(null)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.onCallAdded(call)

        // Launch in-call fullscreen UI for incoming or outgoing calls
        val intent = Intent(this, InCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallManager.onCallRemoved(call)
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        CallManager.onCallAudioStateChanged(audioState)
    }
}
