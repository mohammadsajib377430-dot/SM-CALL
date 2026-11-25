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
    fun rejectsPhoneOnlyAccount() {
        assertFalse(VerifiedAccountPolicy.isVerified(emailVerified = false, phoneNumber = "+8801712345678"))
    }

    @Test
    fun rejectsUnverifiedAccountEvenIfPhoneNumberExists() {
        assertFalse(VerifiedAccountPolicy.isVerified(emailVerified = false, phoneNumber = "+8801712345678"))
    }

    @Test
    fun rejectsUnverifiedAccountWithoutPhoneNumber() {
        assertFalse(VerifiedAccountPolicy.isVerified(emailVerified = false, phoneNumber = null))
    }
}
