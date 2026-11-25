package com.smcall.app

/** SM CALL accepts only Firebase email/password accounts with verified email. */
internal object VerifiedAccountPolicy {
    fun isVerified(emailVerified: Boolean, phoneNumber: String?): Boolean = emailVerified
}
