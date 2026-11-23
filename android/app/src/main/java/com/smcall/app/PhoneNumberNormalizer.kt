package com.smcall.app

/**
 * Normalizes numerals commonly used in Bengali, Arabic, Persian and Hindi UI.
 * Phone numbers must still include an international +country-code prefix.
 */
object PhoneNumberNormalizer {
    fun normalizeDigits(input: String): String = buildString(input.length) {
        input.forEach { character ->
            append(asciiDigit(character) ?: character)
        }
    }

    fun normalizePhone(input: String): String = buildString(input.length) {
        input.trim().forEach { character ->
            val digit = asciiDigit(character)
            when {
                digit != null -> append(digit)
                character == '+' && isEmpty() -> append(character)
                character.isWhitespace() || character in "-()." -> Unit
                else -> append(character)
            }
        }
    }

    fun isValidE164(phone: String): Boolean =
        phone.matches(Regex("^\\+[1-9][0-9]{7,14}$"))

    private fun asciiDigit(character: Char): Char? = when (character) {
        in '০'..'৯' -> ('0'.code + character.code - '০'.code).toChar()
        in '٠'..'٩' -> ('0'.code + character.code - '٠'.code).toChar()
        in '۰'..'۹' -> ('0'.code + character.code - '۰'.code).toChar()
        in '०'..'९' -> ('0'.code + character.code - '०'.code).toChar()
        in '0'..'9' -> character
        else -> null
    }
}
