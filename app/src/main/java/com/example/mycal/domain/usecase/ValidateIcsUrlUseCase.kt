package com.example.mycal.domain.usecase

import javax.inject.Inject

class ValidateIcsUrlUseCase @Inject constructor() {

    operator fun invoke(url: String): ValidationResult {
        return when {
            url.isBlank() -> {
                ValidationResult.Error("URL cannot be empty")
            }
            !url.startsWith("https://") -> {
                ValidationResult.Error("Only HTTPS URLs are allowed for security")
            }
            !isValidUrl(url) -> {
                ValidationResult.Error("Invalid URL format")
            }
            !url.endsWith(".ics") && !url.contains("/calendar") && !url.contains("webcal") -> {
                ValidationResult.Warning("URL might not be a valid calendar feed")
            }
            else -> {
                ValidationResult.Success
            }
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val urlRegex = Regex("^https://[\\w.-]+(?:\\.[\\w\\.-]+)+[\\w\\-\\._~:/?#\\[\\]@!$&'()*+,;=.]+$")
            urlRegex.matches(url)
        } catch (e: Exception) {
            false
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Warning(val message: String) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}