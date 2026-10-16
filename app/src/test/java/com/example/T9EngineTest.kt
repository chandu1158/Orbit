package com.example

import com.example.data.model.ContactItem
import com.example.data.model.ContactNumber
import com.example.ui.screens.keypad.T9Engine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class T9EngineTest {

    @Test
    fun testConvertNameToT9() {
        assertEquals("5646", T9Engine.convertNameToT9("John"))
        assertEquals("262", T9Engine.convertNameToT9("Bob"))
        assertEquals("25423", T9Engine.convertNameToT9("Alice"))
    }

    @Test
    fun testSearchContactsByT9Name() {
        val contacts = listOf(
            ContactItem(
                contactId = 1,
                lookupKey = "k1",
                displayName = "John Doe",
                photoUri = null,
                photoThumbnailUri = null,
                numbers = listOf(ContactNumber(number = "5551234", typeLabel = "mobile"))
            ),
            ContactItem(
                contactId = 2,
                lookupKey = "k2",
                displayName = "Alice Smith",
                photoUri = null,
                photoThumbnailUri = null,
                numbers = listOf(ContactNumber(number = "5559876", typeLabel = "mobile"))
            )
        )

        // "5646" corresponds to "JOHN"
        val results = T9Engine.searchContacts("5646", contacts)
        assertEquals(1, results.size)
        assertEquals("John Doe", results[0].displayName)
    }

    @Test
    fun testSearchContactsByPhoneNumber() {
        val contacts = listOf(
            ContactItem(
                contactId = 1,
                lookupKey = "k1",
                displayName = "John Doe",
                photoUri = null,
                photoThumbnailUri = null,
                numbers = listOf(ContactNumber(number = "5551234", typeLabel = "mobile"))
            )
        )

        val results = T9Engine.searchContacts("1234", contacts)
        assertEquals(1, results.size)
        assertEquals("John Doe", results[0].displayName)
    }
}
