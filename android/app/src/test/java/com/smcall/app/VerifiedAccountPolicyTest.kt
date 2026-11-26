package com.smcall.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifiedAccountPolicyTest {
    @Test
    fun acceptsFirebaseVerifiedEmailAccount() {
        assertTrue(VerifiedAccountPolicy.isVerified(emailVerified = true, phoneNumber = null))
    }

    @Test
    fun acceptsFirebaseVerifiedPhoneAccount() {
        assertTrue(VerifiedAccountPolicy.isVerified(emailVerified = false, phoneNumber = "+8801712345678"))
    }

    @Test
    fun rejectsAccountWithoutVerifiedEmailOrPhone() {
        assertFalse(VerifiedAccountPolicy.isVerified(emailVerified = false, phoneNumber = null))
    }

    @Test
    fun rejectsBlankPhoneNumberWhenEmailIsNotVerified() {
        assertFalse(VerifiedAccountPolicy.isVerified(emailVerified = false, phoneNumber = "  "))
    }
}
