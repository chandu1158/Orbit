package com.example.data.repository

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.model.ContactItem
import com.example.data.model.ContactNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {

    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getContacts(): List<ContactItem> {
        if (!hasContactsPermission()) return emptyList()

        val contactMap = linkedMapOf<Long, MutableList<ContactNumber>>()
        val contactMetaMap = mutableMapOf<Long, Pair<String, Pair<String?, String?>>>()
        val starredMap = mutableMapOf<Long, Boolean>()
        val lookupMap = mutableMapOf<Long, String>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED
        )

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC"

        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val lookupIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
            val labelIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
            val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val thumbIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
            val starredIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)

            while (it.moveToNext()) {
                val contactId = it.getLong(idIndex)
                val lookupKey = if (lookupIndex >= 0) it.getString(lookupIndex) ?: "" else ""
                val name = (if (nameIndex >= 0) it.getString(nameIndex) else null) ?: "Unknown"
                val rawNumber = if (numberIndex >= 0) it.getString(numberIndex) ?: "" else ""
                val type = if (typeIndex >= 0) it.getInt(typeIndex) else ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
                val customLabel = if (labelIndex >= 0) it.getString(labelIndex) else null
                val photoUri = if (photoIndex >= 0) it.getString(photoIndex) else null
                val thumbUri = if (thumbIndex >= 0) it.getString(thumbIndex) else null
                val isStarred = if (starredIndex >= 0) it.getInt(starredIndex) == 1 else false

                val typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    context.resources,
                    type,
                    customLabel
                ).toString()

                if (rawNumber.isNotBlank()) {
                    val numbersList = contactMap.getOrPut(contactId) { mutableListOf() }
                    // Prevent duplicate entries for the same contact
                    if (numbersList.none { cn -> cn.number.replace("\\s+".toRegex(), "") == rawNumber.replace("\\s+".toRegex(), "") }) {
                        numbersList.add(ContactNumber(number = rawNumber, typeLabel = typeLabel, isPrimary = numbersList.isEmpty()))
                    }
                }

                contactMetaMap[contactId] = Pair(name, Pair(photoUri, thumbUri))
                starredMap[contactId] = isStarred
                lookupMap[contactId] = lookupKey
            }
        }

        return contactMap.map { (id, numbers) ->
            val meta = contactMetaMap[id]
            val name = meta?.first ?: "Unknown"
            val photo = meta?.second?.first
            val thumb = meta?.second?.second
            val isStarred = starredMap[id] ?: false
            val lookupKey = lookupMap[id] ?: ""
            ContactItem(
                contactId = id,
                lookupKey = lookupKey,
                displayName = name,
                photoUri = photo,
                photoThumbnailUri = thumb,
                numbers = numbers,
                isStarred = isStarred
            )
        }
    }

    fun getContactsFlow(): Flow<List<ContactItem>> = flow {
        emit(getContacts())
    }.flowOn(Dispatchers.IO)

    suspend fun findContactByNumber(number: String): ContactItem? = withContext(Dispatchers.IO) {
        if (!hasContactsPermission()) return@withContext null
        val normalizedQuery = number.filter { it.isDigit() }
        if (normalizedQuery.isEmpty()) return@withContext null

        val allContacts = getContacts()
        allContacts.firstOrNull { contact ->
            contact.numbers.any { num ->
                val norm = num.number.filter { it.isDigit() }
                norm.endsWith(normalizedQuery) || normalizedQuery.endsWith(norm)
            }
        }
    }
}
