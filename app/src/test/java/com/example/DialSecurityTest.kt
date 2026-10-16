package com.example

import com.example.telecom.DialSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DialSecurityTest {

    @Test
    fun testPostDialDtmfInjectionStripping() {
        // Pauses (,) and waits (;) used for IVR / automated PIN exploitation must be stripped
        val resultComma = DialSanitizer.sanitizeExternalTelUri("1800123456,,9999#")
        assertEquals("1800123456", resultComma.cleanNumber)
        assertTrue(resultComma.hasPostDialDtmf)
        assertTrue(resultComma.wasSanitized)
        assertNotNull(resultComma.warningMessage)

        val resultSemicolon = DialSanitizer.sanitizeExternalTelUri("18005551234;1234")
        assertEquals("18005551234", resultSemicolon.cleanNumber)
        assertTrue(resultSemicolon.hasPostDialDtmf)
    }

    @Test
    fun testUssdAndMmiCodeDetection() {
        // Service codes like *#06#, *21*...# should be flagged so they cannot be dialed silently
        val resultImei = DialSanitizer.sanitizeExternalTelUri("*#06#")
        assertEquals("*#06#", resultImei.cleanNumber)
        assertTrue(resultImei.isUssdOrMmi)
        assertNotNull(resultImei.warningMessage)

        val resultForward = DialSanitizer.sanitizeExternalTelUri("*21*1234567890#")
        assertTrue(resultForward.isUssdOrMmi)

        val normalNumber = DialSanitizer.sanitizeExternalTelUri("+14155552671")
        assertFalse(normalNumber.isUssdOrMmi)
        assertFalse(normalNumber.hasPostDialDtmf)
    }

    @Test
    fun testMalformedAndOversizedInputFuzzing() {
        // 500-character malicious buffer string
        val longString = "12345".repeat(100)
        val result = DialSanitizer.sanitizeExternalTelUri(longString)
        assertTrue(result.cleanNumber.length <= 40)
        assertTrue(result.wasSanitized)

        // Weird unicode and control characters
        val dirty = "123\u000045\n\r67\u202E89"
        val cleanResult = DialSanitizer.sanitizeExternalTelUri(dirty)
        assertEquals("123456789", cleanResult.cleanNumber)
    }

    @Test
    fun testEmergencyNumbersNeverBlocked() {
        // Emergency numbers must NEVER be blocked even if entered in blocklist
        assertTrue(DialSanitizer.isEmergencyNumber("911"))
        assertTrue(DialSanitizer.isEmergencyNumber("112"))
        assertTrue(DialSanitizer.isEmergencyNumber("999"))
        assertTrue(DialSanitizer.isEmergencyNumber("100"))

        assertFalse(DialSanitizer.isEmergencyNumber("5551234"))

        // Even if an emergency number is in the blocklist, isBlockedMatch must refuse to block it
        assertFalse(DialSanitizer.isBlockedMatch("911", "911"))
        assertFalse(DialSanitizer.isBlockedMatch("112", "112"))
    }

    @Test
    fun testNumberNormalizationMatching() {
        // Matching across formats: +91 9876543210 vs 09876543210 vs 9876543210
        val blocked = "+91 9876543210"
        val candidateWithZero = "09876543210"
        val candidatePlain = "9876543210"
        val differentNumber = "9876543211"

        assertTrue(DialSanitizer.isBlockedMatch(blocked, candidateWithZero))
        assertTrue(DialSanitizer.isBlockedMatch(blocked, candidatePlain))
        assertFalse(DialSanitizer.isBlockedMatch(blocked, differentNumber))
    }
}
