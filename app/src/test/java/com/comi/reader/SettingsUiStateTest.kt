package com.comi.reader

import com.comi.reader.domain.model.ReadingMode
import com.comi.reader.domain.model.ThemeMode
import com.comi.reader.ui.settings.SettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUiStateTest {

    @Test
    fun `SettingsUiState defaults`() {
        val state = SettingsUiState()
        assertEquals(ThemeMode.SYSTEM, state.themeMode)
        assertFalse(state.amoledDark)
        assertTrue(state.dynamicColors)
        assertEquals(ReadingMode.LEFT_TO_RIGHT, state.defaultReadingMode)
        assertTrue(state.showPageNumber)
        assertTrue(state.keepScreenOn)
        assertTrue(state.fullscreen)
        assertTrue(state.animateTransitions)
        assertTrue(state.doubleTapZoom)
        assertFalse(state.doublePageMode)
        assertFalse(state.downloadWifiOnly)
        assertFalse(state.incognitoMode)
        assertFalse(state.appLockEnabled)
        assertFalse(state.autoBackupEnabled)
        assertTrue(state.checkUpdates)
    }

    @Test
    fun `SettingsUiState copy with changes`() {
        val state = SettingsUiState().copy(
            themeMode = ThemeMode.DARK,
            amoledDark = true,
            incognitoMode = true
        )
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertTrue(state.amoledDark)
        assertTrue(state.incognitoMode)
    }
}
