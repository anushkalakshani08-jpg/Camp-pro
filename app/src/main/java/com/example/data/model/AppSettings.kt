package com.example.data.model

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class AppPalette {
    NATURE_GREEN,
    OCEAN_BLUE,
    SUNSET_ORANGE,
    WOOD_EARTH
}

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SINHALA("si", "සිංහල")
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val palette: AppPalette = AppPalette.NATURE_GREEN,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val businessName: String = "WildCamp Outdoor Rentals",
    val businessAddress: String = "124 Lake Road, Kandy / Colombo, Sri Lanka",
    val businessPhone: String = "+94 77 123 4567",
    val businessEmail: String = "info@wildcamprentals.com",
    val termsAndConditions: String = "1. Refundable deposit is returned upon inspection.\n2. Late returns incur a daily charge equal to standard rental rate.\n3. Equipment damage will be deducted from deposit.",
    val businessLogoPath: String? = null
)
