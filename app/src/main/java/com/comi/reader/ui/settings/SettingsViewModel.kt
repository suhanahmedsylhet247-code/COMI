package com.comi.reader.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.data.preferences.AppPreferences
import com.comi.reader.domain.model.ReadingMode
import com.comi.reader.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val amoledDark: Boolean = false,
    val dynamicColors: Boolean = true,
    val defaultReadingMode: ReadingMode = ReadingMode.LEFT_TO_RIGHT,
    val showPageNumber: Boolean = true,
    val keepScreenOn: Boolean = true,
    val fullscreen: Boolean = true,
    val animateTransitions: Boolean = true,
    val doubleTapZoom: Boolean = true,
    val doublePageMode: Boolean = false,
    val downloadWifiOnly: Boolean = false,
    val incognitoMode: Boolean = false,
    val appLockEnabled: Boolean = false,
    val autoBackupEnabled: Boolean = false,
    val checkUpdates: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeAll()
    }

    private fun observeAll() {
        viewModelScope.launch { preferences.themeMode.collect { _uiState.value = _uiState.value.copy(themeMode = it) } }
        viewModelScope.launch { preferences.amoledDark.collect { _uiState.value = _uiState.value.copy(amoledDark = it) } }
        viewModelScope.launch { preferences.dynamicColors.collect { _uiState.value = _uiState.value.copy(dynamicColors = it) } }
        viewModelScope.launch { preferences.defaultReadingMode.collect { _uiState.value = _uiState.value.copy(defaultReadingMode = it) } }
        viewModelScope.launch { preferences.showPageNumber.collect { _uiState.value = _uiState.value.copy(showPageNumber = it) } }
        viewModelScope.launch { preferences.keepScreenOn.collect { _uiState.value = _uiState.value.copy(keepScreenOn = it) } }
        viewModelScope.launch { preferences.fullscreen.collect { _uiState.value = _uiState.value.copy(fullscreen = it) } }
        viewModelScope.launch { preferences.animatePageTransitions.collect { _uiState.value = _uiState.value.copy(animateTransitions = it) } }
        viewModelScope.launch { preferences.doubleTapZoom.collect { _uiState.value = _uiState.value.copy(doubleTapZoom = it) } }
        viewModelScope.launch { preferences.doublePageMode.collect { _uiState.value = _uiState.value.copy(doublePageMode = it) } }
        viewModelScope.launch { preferences.downloadWifiOnly.collect { _uiState.value = _uiState.value.copy(downloadWifiOnly = it) } }
        viewModelScope.launch { preferences.incognitoMode.collect { _uiState.value = _uiState.value.copy(incognitoMode = it) } }
        viewModelScope.launch { preferences.appLockEnabled.collect { _uiState.value = _uiState.value.copy(appLockEnabled = it) } }
        viewModelScope.launch { preferences.autoBackupEnabled.collect { _uiState.value = _uiState.value.copy(autoBackupEnabled = it) } }
        viewModelScope.launch { preferences.checkUpdatesEnabled.collect { _uiState.value = _uiState.value.copy(checkUpdates = it) } }
    }

    fun setThemeMode(mode: ThemeMode) { viewModelScope.launch { preferences.setThemeMode(mode) } }
    fun setAmoledDark(enabled: Boolean) { viewModelScope.launch { preferences.setAmoledDark(enabled) } }
    fun setDynamicColors(enabled: Boolean) { viewModelScope.launch { preferences.setDynamicColors(enabled) } }
    fun setDefaultReadingMode(mode: ReadingMode) { viewModelScope.launch { preferences.setDefaultReadingMode(mode) } }
    fun setShowPageNumber(show: Boolean) { viewModelScope.launch { preferences.setShowPageNumber(show) } }
    fun setKeepScreenOn(enabled: Boolean) { viewModelScope.launch { preferences.setKeepScreenOn(enabled) } }
    fun setFullscreen(enabled: Boolean) { viewModelScope.launch { preferences.setFullscreen(enabled) } }
    fun setAnimateTransitions(enabled: Boolean) { viewModelScope.launch { preferences.setAnimatePageTransitions(enabled) } }
    fun setDoubleTapZoom(enabled: Boolean) { viewModelScope.launch { preferences.setDoubleTapZoom(enabled) } }
    fun setDoublePageMode(enabled: Boolean) { viewModelScope.launch { preferences.setDoublePageMode(enabled) } }
    fun setDownloadWifiOnly(enabled: Boolean) { viewModelScope.launch { preferences.setDownloadWifiOnly(enabled) } }
    fun setIncognitoMode(enabled: Boolean) { viewModelScope.launch { preferences.setIncognitoMode(enabled) } }
    fun setAppLockEnabled(enabled: Boolean) { viewModelScope.launch { preferences.setAppLockEnabled(enabled) } }
    fun setAutoBackupEnabled(enabled: Boolean) { viewModelScope.launch { preferences.setAutoBackupEnabled(enabled) } }
    fun setCheckUpdates(enabled: Boolean) { viewModelScope.launch { preferences.setCheckUpdatesEnabled(enabled) } }
}
