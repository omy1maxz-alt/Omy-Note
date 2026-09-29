package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_notes_settings")

data class AppSettings(
    val theme: String = "system", // "system", "light", "dark"
    val font: String = "sans", // "sans", "serif", "mono"
    val defaultTint: String = "cream",
    val sortOrder: String = "custom", // "custom", "modified_desc", "modified_asc", "created_desc", "title_asc"
    val dateFormat: String = "dd/MM/yyyy",
    val displayUpdatedDate: Boolean = true,
    val startDayOfWeek: String = "Monday",
    val geminiEnabled: Boolean = false,
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-2.5-flash",
    val autoTagAndCategorize: Boolean = true,
    val smartSearchEnabled: Boolean = false,
    val clipboardDetection: Boolean = true,
    val appLockEnabled: Boolean = false,
    val skipSplash: Boolean = false,
    val premiumDemo: Boolean = false
)

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val FONT = stringPreferencesKey("font")
        val DEFAULT_TINT = stringPreferencesKey("default_tint")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val DISPLAY_UPDATED_DATE = booleanPreferencesKey("display_updated_date")
        val START_DAY_OF_WEEK = stringPreferencesKey("start_day_of_week")
        val GEMINI_ENABLED = booleanPreferencesKey("gemini_enabled")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val AUTO_TAG_CATEGORIZE = booleanPreferencesKey("auto_tag_categorize")
        val SMART_SEARCH_ENABLED = booleanPreferencesKey("smart_search_enabled")
        val CLIPBOARD_DETECTION = booleanPreferencesKey("clipboard_detection")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val SKIP_SPLASH = booleanPreferencesKey("skip_splash")
        val PREMIUM_DEMO = booleanPreferencesKey("premium_demo")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            theme = prefs[PreferencesKeys.THEME] ?: "system",
            font = prefs[PreferencesKeys.FONT] ?: "sans",
            defaultTint = prefs[PreferencesKeys.DEFAULT_TINT] ?: "cream",
            sortOrder = prefs[PreferencesKeys.SORT_ORDER] ?: "custom",
            dateFormat = prefs[PreferencesKeys.DATE_FORMAT] ?: "dd/MM/yyyy",
            displayUpdatedDate = prefs[PreferencesKeys.DISPLAY_UPDATED_DATE] ?: true,
            startDayOfWeek = prefs[PreferencesKeys.START_DAY_OF_WEEK] ?: "Monday",
            geminiEnabled = prefs[PreferencesKeys.GEMINI_ENABLED] ?: false,
            geminiApiKey = prefs[PreferencesKeys.GEMINI_API_KEY] ?: "",
            geminiModel = prefs[PreferencesKeys.GEMINI_MODEL] ?: "gemini-2.5-flash",
            autoTagAndCategorize = prefs[PreferencesKeys.AUTO_TAG_CATEGORIZE] ?: true,
            smartSearchEnabled = prefs[PreferencesKeys.SMART_SEARCH_ENABLED] ?: false,
            clipboardDetection = prefs[PreferencesKeys.CLIPBOARD_DETECTION] ?: true,
            appLockEnabled = prefs[PreferencesKeys.APP_LOCK_ENABLED] ?: false,
            skipSplash = prefs[PreferencesKeys.SKIP_SPLASH] ?: false,
            premiumDemo = prefs[PreferencesKeys.PREMIUM_DEMO] ?: false
        )
    }

    suspend fun updateTheme(theme: String) {
        context.dataStore.edit { it[PreferencesKeys.THEME] = theme }
    }

    suspend fun updateFont(font: String) {
        context.dataStore.edit { it[PreferencesKeys.FONT] = font }
    }

    suspend fun updateDefaultTint(tint: String) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_TINT] = tint }
    }

    suspend fun updateSortOrder(sortOrder: String) {
        context.dataStore.edit { it[PreferencesKeys.SORT_ORDER] = sortOrder }
    }

    suspend fun updateDateFormat(format: String) {
        context.dataStore.edit { it[PreferencesKeys.DATE_FORMAT] = format }
    }

    suspend fun updateDisplayUpdatedDate(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DISPLAY_UPDATED_DATE] = show }
    }

    suspend fun updateStartDayOfWeek(day: String) {
        context.dataStore.edit { it[PreferencesKeys.START_DAY_OF_WEEK] = day }
    }

    suspend fun updateGeminiEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.GEMINI_ENABLED] = enabled }
    }

    suspend fun updateGeminiApiKey(key: String) {
        context.dataStore.edit { it[PreferencesKeys.GEMINI_API_KEY] = key.trim() }
    }

    suspend fun updateGeminiModel(model: String) {
        context.dataStore.edit { it[PreferencesKeys.GEMINI_MODEL] = model.trim() }
    }

    suspend fun updateAutoTagAndCategorize(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_TAG_CATEGORIZE] = enabled }
    }

    suspend fun updateSmartSearchEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SMART_SEARCH_ENABLED] = enabled }
    }

    suspend fun updateClipboardDetection(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CLIPBOARD_DETECTION] = enabled }
    }

    suspend fun updateAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.APP_LOCK_ENABLED] = enabled }
    }

    suspend fun updateSkipSplash(skip: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SKIP_SPLASH] = skip }
    }

    suspend fun updatePremiumDemo(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PREMIUM_DEMO] = enabled }
    }
}
