package com.smcall.app

/**
 * A Firebase account is verified when Firebase confirms either its email
 * address or its phone number through the corresponding sign-in provider.
 */
internal object VerifiedAccountPolicy {
    fun isVerified(emailVerified: Boolean, phoneNumber: String?): Boolean =
        emailVerified || !phoneNumber.isNullOrBlank()
}
