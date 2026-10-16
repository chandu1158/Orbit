package com.example.data.model

data class CallLogItem(
    val id: Long,
    val number: String,
    val formattedNumber: String,
    val cachedName: String?,
    val type: CallType,
    val timestamp: Long,
    val durationSeconds: Long,
    val callCount: Int = 1,
    val simSlot: Int = -1,
    val contactLookupUri: String? = null
)
