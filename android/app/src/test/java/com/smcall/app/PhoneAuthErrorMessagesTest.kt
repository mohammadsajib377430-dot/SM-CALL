package com.smcall.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class PhoneAuthErrorMessagesTest {
    @Test
    fun mapsBillingNotEnabledInAllFourLanguages() {
        val messages = listOf("bn", "en", "ar", "hi").map { language ->
            PhoneAuthErrorMessages.message(
                "ERROR_BILLING_NOT_ENABLED",
                "FirebaseAuthException",
                language
            )
        }

        messages.forEach { assertNotNull(it) }
        assertEquals(4, messages.distinct().size)
    }

    @Test
    fun acceptsFirebaseNamespacedErrorCodes() {
        val message = PhoneAuthErrorMessages.message(
            "auth/operation-not-allowed",
            "FirebaseAuthException",
            "en"
        )

        assertEquals(
            "Phone sign-in is disabled in Firebase Authentication. Enable the Phone provider.",
            message
        )
    }

    @Test
    fun recognizesSmsRateLimitExceptionWithoutAnAuthErrorCode() {
        val message = PhoneAuthErrorMessages.message(
            null,
            "com.google.firebase.FirebaseTooManyRequestsException",
            "bn"
        )

        assertEquals(
            "অনেকবার চেষ্টা হয়েছে। নতুন OTP চাওয়ার আগে কিছুক্ষণ অপেক্ষা করুন।",
            message
        )
    }

    @Test
    fun mapsExpiredAndIncorrectCodesSeparately() {
        val expired = PhoneAuthErrorMessages.message(
            "ERROR_SESSION_EXPIRED",
            "FirebaseAuthException",
            "hi"
        )
        val incorrect = PhoneAuthErrorMessages.message(
            "ERROR_INVALID_VERIFICATION_CODE",
            "FirebaseAuthException",
            "hi"
        )

        assertNotNull(expired)
        assertNotNull(incorrect)
        assertEquals(false, expired == incorrect)
    }

    @Test
    fun returnsNullForUnknownErrorsSoDiagnosticFallbackCanBeUsed() {
        assertNull(
            PhoneAuthErrorMessages.message(
                "ERROR_SOMETHING_NEW",
                "FirebaseAuthException",
                "bn"
            )
        )
    }

    @Test
    fun defaultsUnknownLanguageToBengali() {
        val message = PhoneAuthErrorMessages.message(
            "ERROR_QUOTA_EXCEEDED",
            "FirebaseAuthException",
            "fr"
        )

        assertEquals(
            "SMS পাঠানোর সীমা শেষ হয়েছে। পরে চেষ্টা করুন অথবা Firebase Test Phone Number ব্যবহার করুন।",
            message
        )
    }
}
