package com.smcall.app

/**
 * Checks the input shape before asking Firebase Phone Authentication to verify it.
 * This does not prove that a number is assigned, reachable, or supported by Firebase.
 */
object PhoneAuthInputValidator {
    private val e164Pattern = Regex("""^\+[1-9][0-9]{7,14}$""")
    private val otpPattern = Regex("""^[0-9]{6}$""")

    fun isValidPhoneNumber(value: String): Boolean = e164Pattern.matches(value)

    fun isValidOtp(value: String): Boolean = otpPattern.matches(value)
}
