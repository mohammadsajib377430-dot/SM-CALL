package com.smcall.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneAuthInputValidatorTest {
    @Test
    fun acceptsInternationalE164NumberShapes() {
        listOf(
            "+8801712345678",
            "+6591234567",
            "+12025550123"
        ).forEach { number ->
            assertTrue("Expected valid E.164 shape: $number", PhoneAuthInputValidator.isValidPhoneNumber(number))
        }
    }

    @Test
    fun normalizesBanglaArabicAndDevanagariPhoneDigits() {
        val examples = listOf(
            "+৮৮০ ১৭১-২৩৪৫৬৭৮",
            "+٨٨٠ ١٧١-٢٣٤٥٦٧٨",
            "+८८० १७१-२३४५६७८"
        )
        examples.forEach { number ->
            assertEquals("+8801712345678", PhoneAuthInputValidator.normalizePhoneNumber(number))
            assertTrue(PhoneAuthInputValidator.isValidPhoneNumber(number))
        }
    }

    @Test
    fun rejectsMalformedPhoneNumberShapes() {
        listOf(
            "01712345678",
            "8801712345678",
            "+0123456789",
            "+1234567",
            "+1234567890123456",
            "+88017ABC5678",
            "+880/1712345678"
        ).forEach { number ->
            assertFalse("Expected invalid E.164 shape: $number", PhoneAuthInputValidator.isValidPhoneNumber(number))
        }
    }

    @Test
    fun acceptsExactlySixAsciiOtpDigits() {
        assertTrue(PhoneAuthInputValidator.isValidOtp("123456"))
    }

    @Test
    fun normalizesLocalizedOtpDigits() {
        listOf("১২৩৪৫৬", "١٢٣٤٥٦", "१२३४५६").forEach { code ->
            assertEquals("123456", PhoneAuthInputValidator.normalizeOtp(code))
            assertTrue(PhoneAuthInputValidator.isValidOtp(code))
        }
    }

    @Test
    fun rejectsOtpWithWrongLengthOrCharacters() {
        listOf(
            "12345",
            "1234567",
            "12A456",
            "123 56"
        ).forEach { code ->
            assertFalse("Expected invalid OTP: $code", PhoneAuthInputValidator.isValidOtp(code))
        }
    }
}
