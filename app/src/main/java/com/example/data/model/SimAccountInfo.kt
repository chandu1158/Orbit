package com.example.data.model

import android.telecom.PhoneAccountHandle

data class SimAccountInfo(
    val slotIndex: Int,
    val displayName: String,
    val carrierName: String,
    val phoneNumber: String?,
    val phoneAccountHandle: PhoneAccountHandle
)
