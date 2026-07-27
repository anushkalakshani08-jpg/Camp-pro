package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.AppPalette
import com.example.data.model.AppSettings
import com.example.data.model.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val paletteStr = prefs.getString("palette", AppPalette.NATURE_GREEN.name) ?: AppPalette.NATURE_GREEN.name
        val langStr = prefs.getString("language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name

        return AppSettings(
            themeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM),
            palette = runCatching { AppPalette.valueOf(paletteStr) }.getOrDefault(AppPalette.NATURE_GREEN),
            language = runCatching { AppLanguage.valueOf(langStr) }.getOrDefault(AppLanguage.ENGLISH),
            businessName = prefs.getString("biz_name", "WildCamp Outdoor Rentals") ?: "WildCamp Outdoor Rentals",
            businessAddress = prefs.getString("biz_address", "124 Lake Road, Kandy / Colombo, Sri Lanka") ?: "124 Lake Road, Kandy / Colombo, Sri Lanka",
            businessPhone = prefs.getString("biz_phone", "+94 77 123 4567") ?: "+94 77 123 4567",
            businessEmail = prefs.getString("biz_email", "info@wildcamprentals.com") ?: "info@wildcamprentals.com",
            termsAndConditions = prefs.getString("biz_terms", "1. Refundable deposit returned on inspection.\n2. Overdue rentals charged at daily rate.") ?: "1. Refundable deposit returned on inspection.\n2. Overdue rentals charged at daily rate.",
            businessLogoPath = prefs.getString("biz_logo_path", null)
        )
    }

    fun updateThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun updatePalette(palette: AppPalette) {
        prefs.edit().putString("palette", palette.name).apply()
        _settings.value = _settings.value.copy(palette = palette)
    }

    fun updateLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.name).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun updateBusinessDetails(
        name: String,
        address: String,
        phone: String,
        email: String,
        terms: String,
        logoPath: String?
    ) {
        prefs.edit()
            .putString("biz_name", name)
            .putString("biz_address", address)
            .putString("biz_phone", phone)
            .putString("biz_email", email)
            .putString("biz_terms", terms)
            .putString("biz_logo_path", logoPath)
            .apply()

        _settings.value = _settings.value.copy(
            businessName = name,
            businessAddress = address,
            businessPhone = phone,
            businessEmail = email,
            termsAndConditions = terms,
            businessLogoPath = logoPath
        )
    }
}
