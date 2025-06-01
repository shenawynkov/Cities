package com.shenawynkov.cities.presentation.ui.util

/**
 * Converts a 2-letter ISO country code to its corresponding Unicode flag emoji.
 *
 * @param countryCode The 2-letter ISO country code (e.g., "US", "DK").
 * @return The Unicode flag emoji string, or "❓" if the code is invalid.
 */
fun countryCodeToEmojiFlag(countryCode: String): String {
    if (countryCode.length != 2) {
        return "❓" // Return a question mark or empty string for invalid codes
    }
    // Ensure the country code consists of uppercase letters only, as regional indicators are based on A-Z.
    if (!countryCode.all { it.isLetter() && it.isUpperCase() }) {
         // Attempt to convert to uppercase if it's mixed or lowercase letters
        val uppercaseCode = countryCode.uppercase()
        if (uppercaseCode.length == 2 && uppercaseCode.all { it.isLetter() && it.isUpperCase() }){
            // If conversion is successful and valid, proceed with the uppercase code
            val codePoints = uppercaseCode.map {
                0x1F1E6 + (it.code - 'A'.code) // Regional Indicator Symbol Letter A is 0x1F1E6
            }
            return String(codePoints.toIntArray(), 0, codePoints.size)
        }
        return "❓" // Fallback if conversion doesn't yield a valid code
    }

    val codePoints = countryCode.map {
        // Regional Indicator Symbol Letter A is 0x1F1E6
        // Each letter of the country code is an offset from this.
        0x1F1E6 + (it.code - 'A'.code)
    }
    return String(codePoints.toIntArray(), 0, codePoints.size)
} 