package com.smcall.app

/**
 * Normalizes localized decimal digits before checking phone and OTP input.
 * This validates input shape only; Firebase still verifies the actual number and code.
 */
object PhoneAuthInputValidator {
    // Raw Kotlin strings do not need a doubled backslash: \\+ in regex would
    // match backslashes, while \\+ here in the source below is a literal plus.
    private val e164Pattern = Regex("""^\+[1-9][0-9]{7,14}$""")
    private val otpPattern = Regex("""^[0-9]{6}$""")

    private fun normalizeDecimalDigits(value: String): String = buildString {
        value.forEach { character ->
            val digit = Character.digit(character, 10)
            append(if (digit in 0..9) ('0'.code + digit).toChar() else character)
        }
    }

    fun normalizePhoneNumber(value: String): String =
        normalizeDecimalDigits(value).trim().filterNot {
            it == ' ' || it == '-' || it == '(' || it == ')'
        }

    fun normalizeOtp(value: String): String =
        normalizeDecimalDigits(value).trim()

    fun isValidPhoneNumber(value: String): Boolean =
        e164Pattern.matches(normalizePhoneNumber(value))

    /**
     * A Firebase verification ID belongs to the phone number that requested it.
     * Compare normalized values so localized digits and harmless separators still match.
     */
    fun matchesVerificationPhone(input: String, verificationPhone: String?): Boolean =
        verificationPhone != null &&
            normalizePhoneNumber(input) == normalizePhoneNumber(verificationPhone) &&
            isValidPhoneNumber(verificationPhone)

    fun isValidOtp(value: String): Boolean =
        otpPattern.matches(normalizeOtp(value))
}
