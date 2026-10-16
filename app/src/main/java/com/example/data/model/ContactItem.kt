package com.example.data.model

data class ContactNumber(
    val number: String,
    val typeLabel: String,
    val isPrimary: Boolean = false
)

data class ContactItem(
    val contactId: Long,
    val lookupKey: String,
    val displayName: String,
    val photoUri: String?,
    val photoThumbnailUri: String?,
    val numbers: List<ContactNumber>,
    val isStarred: Boolean = false,
    val initial: String = displayName.firstOrNull()?.uppercase() ?: "#"
)
