package com.example.ui.screens.keypad

import com.example.data.model.ContactItem

object T9Engine {
    private val charToDigitMap = mapOf(
        'a' to '2', 'b' to '2', 'c' to '2',
        'd' to '3', 'e' to '3', 'f' to '3',
        'g' to '4', 'h' to '4', 'i' to '4',
        'j' to '5', 'k' to '5', 'l' to '5',
        'm' to '6', 'n' to '6', 'o' to '6',
        'p' to '7', 'q' to '7', 'r' to '7', 's' to '7',
        't' to '8', 'u' to '8', 'v' to '8',
        'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9'
    )

    fun convertNameToT9(name: String): String {
        val sb = StringBuilder()
        for (char in name.lowercase()) {
            val digit = charToDigitMap[char]
            if (digit != null) {
                sb.append(digit)
            } else if (char.isWhitespace()) {
                sb.append(' ')
            }
        }
        return sb.toString()
    }

    fun searchContacts(query: String, contacts: List<ContactItem>): List<ContactItem> {
        val digitsOnly = query.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return emptyList()

        return contacts.mapNotNull { contact ->
            // Check direct number match
            val numberMatch = contact.numbers.any { num ->
                num.number.filter { it.isDigit() }.contains(digitsOnly)
            }
            if (numberMatch) {
                return@mapNotNull Pair(contact, 1) // High priority
            }

            // Check T9 name match
            val t9Name = convertNameToT9(contact.displayName)
            val words = t9Name.split(' ')
            val wordPrefixMatch = words.any { it.startsWith(digitsOnly) }
            val containsMatch = t9Name.replace(" ", "").contains(digitsOnly)

            when {
                wordPrefixMatch -> Pair(contact, 2)
                containsMatch -> Pair(contact, 3)
                else -> null
            }
        }.sortedBy { it.second }
            .map { it.first }
            .take(6)
    }
}
