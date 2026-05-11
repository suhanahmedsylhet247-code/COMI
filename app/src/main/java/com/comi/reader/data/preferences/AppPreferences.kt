package com.comi.reader.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.comi.reader.domain.model.ColorFilterMode
import com.comi.reader.domain.model.ReadingMode
import com.comi.reader.domain.model.RotationMode
import com.comi.reader.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "comi_settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Appearance
    private val THEME_MODE = intPreferencesKey("theme_mode")
    private val AMOLED_DARK = booleanPreferencesKey("amoled_dark")
    private val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")

    // Reader
    private val DEFAULT_READING_MODE = intPreferencesKey("default_reading_mode")
    private val SHOW_PAGE_NUMBER = booleanPreferencesKey("show_page_number")
    private val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    private val FULLSCREEN = booleanPreferencesKey("fullscreen")
    private val ANIMATE_PAGE_TRANSITIONS = booleanPreferencesKey("animate_page_transitions")
    private val DOUBLE_TAP_ZOOM = booleanPreferencesKey("double_tap_zoom")
    private val ROTATION_MODE = intPreferencesKey("rotation_mode")
    private val BRIGHTNESS_OVERRIDE = floatPreferencesKey("brightness_override")
    private val USE_CUSTOM_BRIGHTNESS = booleanPreferencesKey("use_custom_brightness")
    private val COLOR_FILTER_MODE = intPreferencesKey("color_filter_mode")
    private val COLOR_FILTER_STRENGTH = floatPreferencesKey("color_filter_strength")
    private val SHOW_TAP_ZONES = booleanPreferencesKey("show_tap_zones")
    private val READER_BACKGROUND = intPreferencesKey("reader_background")
    private val DOUBLE_PAGE_MODE = booleanPreferencesKey("double_page_mode")
    private val CUTOUT_SHORT_DISPLAY = booleanPreferencesKey("cutout_short_display")

    // Downloads
    private val DOWNLOAD_WIFI_ONLY = booleanPreferencesKey("download_wifi_only")
    private val DOWNLOAD_LOCATION = stringPreferencesKey("download_location")
    private val CONCURRENT_DOWNLOADS = intPreferencesKey("concurrent_downloads")

    // Library
    private val LIBRARY_DISPLAY_MODE = intPreferencesKey("library_display_mode")
    private val LIBRARY_SORT_MODE = intPreferencesKey("library_sort_mode")
    private val LIBRARY_COLUMNS = intPreferencesKey("library_columns")

    // Tracking
    private val MAL_TOKEN = stringPreferencesKey("mal_token")
    private val ANILIST_TOKEN = stringPreferencesKey("anilist_token")
    private val KITSU_TOKEN = stringPreferencesKey("kitsu_token")

    // Privacy
    private val INCOGNITO_MODE = booleanPreferencesKey("incognito_mode")
    private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    private val APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")

    // Backup
    private val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
    private val AUTO_BACKUP_INTERVAL = intPreferencesKey("auto_backup_interval_hours")
    private val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")

    // Updates
    private val CHECK_UPDATES_ENABLED = booleanPreferencesKey("check_updates_enabled")
    private val UPDATE_INTERVAL_HOURS = intPreferencesKey("update_interval_hours")

    // Statistics
    private val TOTAL_CHAPTERS_READ = intPreferencesKey("total_chapters_read")
    private val TOTAL_READING_TIME_MINS = longPreferencesKey("total_reading_time_mins")
    private val READING_STREAK_DAYS = intPreferencesKey("reading_streak_days")
    private val LAST_READ_DATE = stringPreferencesKey("last_read_date")

    // Theme
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromOrdinal(prefs[THEME_MODE] ?: 0)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.ordinal }
    }

    val amoledDark: Flow<Boolean> = dataStore.data.map { it[AMOLED_DARK] ?: false }

    suspend fun setAmoledDark(enabled: Boolean) {
        dataStore.edit { it[AMOLED_DARK] = enabled }
    }

    val dynamicColors: Flow<Boolean> = dataStore.data.map { it[DYNAMIC_COLORS] ?: true }

    suspend fun setDynamicColors(enabled: Boolean) {
        dataStore.edit { it[DYNAMIC_COLORS] = enabled }
    }

    // Reader settings
    val defaultReadingMode: Flow<ReadingMode> = dataStore.data.map { prefs ->
        ReadingMode.entries.getOrElse(prefs[DEFAULT_READING_MODE] ?: 0) { ReadingMode.LEFT_TO_RIGHT }
    }

    suspend fun setDefaultReadingMode(mode: ReadingMode) {
        dataStore.edit { it[DEFAULT_READING_MODE] = mode.ordinal }
    }

    val showPageNumber: Flow<Boolean> = dataStore.data.map { it[SHOW_PAGE_NUMBER] ?: true }

    suspend fun setShowPageNumber(show: Boolean) {
        dataStore.edit { it[SHOW_PAGE_NUMBER] = show }
    }

    val keepScreenOn: Flow<Boolean> = dataStore.data.map { it[KEEP_SCREEN_ON] ?: true }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        dataStore.edit { it[KEEP_SCREEN_ON] = enabled }
    }

    val fullscreen: Flow<Boolean> = dataStore.data.map { it[FULLSCREEN] ?: true }

    suspend fun setFullscreen(enabled: Boolean) {
        dataStore.edit { it[FULLSCREEN] = enabled }
    }

    val animatePageTransitions: Flow<Boolean> = dataStore.data.map { it[ANIMATE_PAGE_TRANSITIONS] ?: true }

    suspend fun setAnimatePageTransitions(enabled: Boolean) {
        dataStore.edit { it[ANIMATE_PAGE_TRANSITIONS] = enabled }
    }

    val doubleTapZoom: Flow<Boolean> = dataStore.data.map { it[DOUBLE_TAP_ZOOM] ?: true }

    suspend fun setDoubleTapZoom(enabled: Boolean) {
        dataStore.edit { it[DOUBLE_TAP_ZOOM] = enabled }
    }

    val rotationMode: Flow<RotationMode> = dataStore.data.map { prefs ->
        RotationMode.entries.getOrElse(prefs[ROTATION_MODE] ?: 0) { RotationMode.FREE }
    }

    suspend fun setRotationMode(mode: RotationMode) {
        dataStore.edit { it[ROTATION_MODE] = mode.ordinal }
    }

    val brightnessOverride: Flow<Float> = dataStore.data.map { it[BRIGHTNESS_OVERRIDE] ?: -1f }

    suspend fun setBrightnessOverride(value: Float) {
        dataStore.edit { it[BRIGHTNESS_OVERRIDE] = value }
    }

    val useCustomBrightness: Flow<Boolean> = dataStore.data.map { it[USE_CUSTOM_BRIGHTNESS] ?: false }

    suspend fun setUseCustomBrightness(enabled: Boolean) {
        dataStore.edit { it[USE_CUSTOM_BRIGHTNESS] = enabled }
    }

    val colorFilterMode: Flow<ColorFilterMode> = dataStore.data.map { prefs ->
        ColorFilterMode.entries.getOrElse(prefs[COLOR_FILTER_MODE] ?: 0) { ColorFilterMode.NONE }
    }

    suspend fun setColorFilterMode(mode: ColorFilterMode) {
        dataStore.edit { it[COLOR_FILTER_MODE] = mode.ordinal }
    }

    val colorFilterStrength: Flow<Float> = dataStore.data.map { it[COLOR_FILTER_STRENGTH] ?: 0.25f }

    suspend fun setColorFilterStrength(strength: Float) {
        dataStore.edit { it[COLOR_FILTER_STRENGTH] = strength }
    }

    val showTapZones: Flow<Boolean> = dataStore.data.map { it[SHOW_TAP_ZONES] ?: false }

    suspend fun setShowTapZones(enabled: Boolean) {
        dataStore.edit { it[SHOW_TAP_ZONES] = enabled }
    }

    val readerBackground: Flow<Int> = dataStore.data.map { it[READER_BACKGROUND] ?: 0 }

    suspend fun setReaderBackground(bg: Int) {
        dataStore.edit { it[READER_BACKGROUND] = bg }
    }

    val doublePageMode: Flow<Boolean> = dataStore.data.map { it[DOUBLE_PAGE_MODE] ?: false }

    suspend fun setDoublePageMode(enabled: Boolean) {
        dataStore.edit { it[DOUBLE_PAGE_MODE] = enabled }
    }

    val cutoutShortDisplay: Flow<Boolean> = dataStore.data.map { it[CUTOUT_SHORT_DISPLAY] ?: true }

    suspend fun setCutoutShortDisplay(enabled: Boolean) {
        dataStore.edit { it[CUTOUT_SHORT_DISPLAY] = enabled }
    }

    // Downloads
    val downloadWifiOnly: Flow<Boolean> = dataStore.data.map { it[DOWNLOAD_WIFI_ONLY] ?: false }

    suspend fun setDownloadWifiOnly(enabled: Boolean) {
        dataStore.edit { it[DOWNLOAD_WIFI_ONLY] = enabled }
    }

    val concurrentDownloads: Flow<Int> = dataStore.data.map { it[CONCURRENT_DOWNLOADS] ?: 2 }

    suspend fun setConcurrentDownloads(count: Int) {
        dataStore.edit { it[CONCURRENT_DOWNLOADS] = count }
    }

    // Privacy
    val incognitoMode: Flow<Boolean> = dataStore.data.map { it[INCOGNITO_MODE] ?: false }

    suspend fun setIncognitoMode(enabled: Boolean) {
        dataStore.edit { it[INCOGNITO_MODE] = enabled }
    }

    val appLockEnabled: Flow<Boolean> = dataStore.data.map { it[APP_LOCK_ENABLED] ?: false }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { it[APP_LOCK_ENABLED] = enabled }
    }

    // Backup
    val autoBackupEnabled: Flow<Boolean> = dataStore.data.map { it[AUTO_BACKUP_ENABLED] ?: false }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        dataStore.edit { it[AUTO_BACKUP_ENABLED] = enabled }
    }

    val autoBackupIntervalHours: Flow<Int> = dataStore.data.map { it[AUTO_BACKUP_INTERVAL] ?: 24 }

    suspend fun setAutoBackupIntervalHours(hours: Int) {
        dataStore.edit { it[AUTO_BACKUP_INTERVAL] = hours }
    }

    val lastBackupTime: Flow<Long> = dataStore.data.map { it[LAST_BACKUP_TIME] ?: 0L }

    suspend fun setLastBackupTime(time: Long) {
        dataStore.edit { it[LAST_BACKUP_TIME] = time }
    }

    // Updates
    val checkUpdatesEnabled: Flow<Boolean> = dataStore.data.map { it[CHECK_UPDATES_ENABLED] ?: true }

    suspend fun setCheckUpdatesEnabled(enabled: Boolean) {
        dataStore.edit { it[CHECK_UPDATES_ENABLED] = enabled }
    }

    val updateIntervalHours: Flow<Int> = dataStore.data.map { it[UPDATE_INTERVAL_HOURS] ?: 12 }

    suspend fun setUpdateIntervalHours(hours: Int) {
        dataStore.edit { it[UPDATE_INTERVAL_HOURS] = hours }
    }

    // Statistics
    val totalChaptersRead: Flow<Int> = dataStore.data.map { it[TOTAL_CHAPTERS_READ] ?: 0 }

    suspend fun incrementChaptersRead() {
        dataStore.edit { prefs -> prefs[TOTAL_CHAPTERS_READ] = (prefs[TOTAL_CHAPTERS_READ] ?: 0) + 1 }
    }

    val totalReadingTimeMins: Flow<Long> = dataStore.data.map { it[TOTAL_READING_TIME_MINS] ?: 0L }

    suspend fun addReadingTime(minutes: Long) {
        dataStore.edit { prefs -> prefs[TOTAL_READING_TIME_MINS] = (prefs[TOTAL_READING_TIME_MINS] ?: 0L) + minutes }
    }

    val readingStreakDays: Flow<Int> = dataStore.data.map { it[READING_STREAK_DAYS] ?: 0 }

    suspend fun setReadingStreak(days: Int) {
        dataStore.edit { it[READING_STREAK_DAYS] = days }
    }

    val lastReadDate: Flow<String> = dataStore.data.map { it[LAST_READ_DATE] ?: "" }

    suspend fun setLastReadDate(date: String) {
        dataStore.edit { it[LAST_READ_DATE] = date }
    }

    // Tracker tokens
    val malToken: Flow<String> = dataStore.data.map { it[MAL_TOKEN] ?: "" }

    suspend fun setMalToken(token: String) {
        dataStore.edit { it[MAL_TOKEN] = token }
    }

    val anilistToken: Flow<String> = dataStore.data.map { it[ANILIST_TOKEN] ?: "" }

    suspend fun setAnilistToken(token: String) {
        dataStore.edit { it[ANILIST_TOKEN] = token }
    }

    val kitsuToken: Flow<String> = dataStore.data.map { it[KITSU_TOKEN] ?: "" }

    suspend fun setKitsuToken(token: String) {
        dataStore.edit { it[KITSU_TOKEN] = token }
    }
}
