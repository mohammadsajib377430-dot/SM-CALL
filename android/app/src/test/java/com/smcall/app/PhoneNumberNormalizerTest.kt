package com.smcall.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberNormalizerTest {
    @Test
    fun normalizesBengaliPhoneNumber() {
        assertEquals("+8801712345678", PhoneNumberNormalizer.normalizePhone("+৮৮০ ১৭১-২৩৪৫৬৭৮"))
    }

    @Test
    fun normalizesArabicAndPersianPhoneDigits() {
        assertEquals("+971501234567", PhoneNumberNormalizer.normalizePhone("+٩٧١ ٥٠١٢٣٤٥٦٧"))
        assertEquals("+989121234567", PhoneNumberNormalizer.normalizePhone("+۹۸۹۱۲۱۲۳۴۵۶۷"))
    }

    @Test
    fun normalizesHindiPhoneDigits() {
        assertEquals("+919876543210", PhoneNumberNormalizer.normalizePhone("+९१ ९८७६५४३२१०"))
    }

    @Test
    fun normalizesOtpDigitsForAllSupportedScripts() {
        assertEquals("123456", PhoneNumberNormalizer.normalizeDigits("১২৩৪৫৬"))
        assertEquals("123456", PhoneNumberNormalizer.normalizeDigits("١٢٣٤٥٦"))
        assertEquals("123456", PhoneNumberNormalizer.normalizeDigits("۱۲۳۴۵۶"))
        assertEquals("123456", PhoneNumberNormalizer.normalizeDigits("१२३४५६"))
    }

    @Test
    fun validatesE164LengthAndPrefix() {
        assertTrue(PhoneNumberNormalizer.isValidE164("+8801712345678"))
        assertFalse(PhoneNumberNormalizer.isValidE164("01712345678"))
        assertFalse(PhoneNumberNormalizer.isValidE164("+012345678"))
        assertFalse(PhoneNumberNormalizer.isValidE164("+1234567"))
        assertFalse(PhoneNumberNormalizer.isValidE164("+1234567890123456"))
    }

    @Test
    fun rejectsUnexpectedCharactersInsteadOfSilentlyRemovingThem() {
        assertFalse(PhoneNumberNormalizer.isValidE164(PhoneNumberNormalizer.normalizePhone("+880abc1712345678")))
    }
}
