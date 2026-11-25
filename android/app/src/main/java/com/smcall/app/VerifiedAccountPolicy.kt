package com.smcall.app

/**
 * A Firebase account is eligible for SM CALL features when Firebase has
 * verified either its email address or its phone number through sign-in.
 *
 * PhoneAuth users have isEmailVerified == false, so checking that property
 * alone incorrectly blocks valid phone-authenticated users.
 */
internal object VerifiedAccountPolicy {
    fun isVerified(emailVerified: Boolean, phoneNumber: String?): Boolean =
        emailVerified || !phoneNumber.isNullOrBlank()
}
