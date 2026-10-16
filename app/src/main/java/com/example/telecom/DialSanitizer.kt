package com.example.telecom

import android.telephony.PhoneNumberUtils

data class SanitizedDialResult(
    val cleanNumber: String,
    val isUssdOrMmi: Boolean = false,
    val hasPostDialDtmf: Boolean = false,
    val wasSanitized: Boolean = false,
    val warningMessage: String? = null
)

object DialSanitizer {
    private val EMERGENCY_NUMBERS = setOf(
        "911", "112", "999", "100", "101", "102", "108", "000", "110", "118", "119"
    )

    fun isEmergencyNumber(number: String?): Boolean {
        if (number.isNullOrBlank()) return false
        val digitsOnly = number.filter { it.isDigit() }
        if (EMERGENCY_NUMBERS.contains(digitsOnly)) return true
        return try {
            PhoneNumberUtils.isEmergencyNumber(digitsOnly)
        } catch (e: Exception) {
            EMERGENCY_NUMBERS.contains(digitsOnly)
        }
    }

    fun sanitizeExternalTelUri(rawInput: String?): SanitizedDialResult {
        if (rawInput.isNullOrBlank()) {
            return SanitizedDialResult(cleanNumber = "")
        }

        var isUssd = false
        var hasPostDial = false
        var wasModified = false

        // Check for post-dial pause (,) and wait (;) characters
        if (rawInput.contains(',') || rawInput.contains(';')) {
            hasPostDial = true
            wasModified = true
        }

        // Take only the part before any post-dial pause or wait
        val beforePause = rawInput.split(',', ';').firstOrNull() ?: ""

        // Check for USSD or MMI code patterns (e.g. starts with * or # and ends with #)
        val trimmed = beforePause.trim()
        if ((trimmed.startsWith("*") || trimmed.startsWith("#")) && trimmed.endsWith("#")) {
            isUssd = true
        }

        // Filter valid phone characters: digits, leading +, *, #
        val sb = StringBuilder()
        for (i in trimmed.indices) {
            val c = trimmed[i]
            if (c.isDigit()) {
                sb.append(c)
            } else if (c == '+' && sb.isEmpty()) {
                sb.append(c)
            } else if ((c == '*' || c == '#') && !isUssd) {
                // Ignore unexpected internal symbols unless USSD
                wasModified = true
            } else if (isUssd && (c == '*' || c == '#')) {
                sb.append(c)
            } else {
                wasModified = true
            }
        }

        // Limit maximum phone number length to 40 characters to prevent buffer issues
        val clean = if (sb.length > 40) {
            wasModified = true
            sb.substring(0, 40)
        } else {
            sb.toString()
        }

        val warning = when {
            isUssd -> "Service/USSD code detected. Review carefully before dialing."
            hasPostDial -> "Automated post-dial DTMF sequences were stripped for security."
            else -> null
        }

        return SanitizedDialResult(
            cleanNumber = clean,
            isUssdOrMmi = isUssd,
            hasPostDialDtmf = hasPostDial,
            wasSanitized = wasModified,
            warningMessage = warning
        )
    }

    fun normalizeForComparison(rawNumber: String?): String {
        if (rawNumber.isNullOrBlank()) return ""
        val digits = rawNumber.filter { it.isDigit() }
        // Take up to the last 10 digits for robust national matching (+91 98xxx vs 098xxx vs 98xxx)
        return if (digits.length > 10) {
            digits.takeLast(10)
        } else {
            digits
        }
    }

    fun isBlockedMatch(blockedRecord: String, candidateNumber: String): Boolean {
        // Safety requirement: Never block emergency numbers!
        if (isEmergencyNumber(candidateNumber)) {
            return false
        }

        val normBlocked = normalizeForComparison(blockedRecord)
        val normCandidate = normalizeForComparison(candidateNumber)

        if (normBlocked.isEmpty() || normCandidate.isEmpty()) {
            return false
        }

        // Exact national suffix match
        if (normBlocked == normCandidate) return true

        // If either has 7 or more digits and ends with the other
        if (normBlocked.length >= 7 && normCandidate.length >= 7) {
            if (normBlocked.endsWith(normCandidate) || normCandidate.endsWith(normBlocked)) {
                return true
            }
        }

        return false
    }
}
