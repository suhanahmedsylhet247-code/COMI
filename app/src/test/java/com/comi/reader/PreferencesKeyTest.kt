package com.comi.reader

import com.comi.reader.domain.model.ColorFilterMode
import com.comi.reader.domain.model.ReadingMode
import com.comi.reader.domain.model.RotationMode
import com.comi.reader.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesKeyTest {

    @Test
    fun `ThemeMode has all expected values`() {
        val modes = ThemeMode.entries
        assertEquals(3, modes.size)
        assertEquals(ThemeMode.SYSTEM, modes[0])
        assertEquals(ThemeMode.LIGHT, modes[1])
        assertEquals(ThemeMode.DARK, modes[2])
    }

    @Test
    fun `ReadingMode has all expected values`() {
        val modes = ReadingMode.entries
        assertEquals(4, modes.size)
        assertEquals(ReadingMode.LEFT_TO_RIGHT, modes[0])
        assertEquals(ReadingMode.RIGHT_TO_LEFT, modes[1])
        assertEquals(ReadingMode.VERTICAL, modes[2])
        assertEquals(ReadingMode.WEBTOON, modes[3])
    }

    @Test
    fun `RotationMode has all expected values`() {
        val modes = RotationMode.entries
        assertEquals(6, modes.size)
        assertEquals(RotationMode.FREE, modes[0])
        assertEquals(RotationMode.PORTRAIT, modes[1])
        assertEquals(RotationMode.LANDSCAPE, modes[2])
    }

    @Test
    fun `ColorFilterMode has all expected values`() {
        val modes = ColorFilterMode.entries
        assertEquals(5, modes.size)
        assertEquals(ColorFilterMode.NONE, modes[0])
        assertEquals(ColorFilterMode.SEPIA, modes[1])
        assertEquals(ColorFilterMode.GRAYSCALE, modes[2])
        assertEquals(ColorFilterMode.NIGHT, modes[3])
        assertEquals(ColorFilterMode.CUSTOM, modes[4])
    }
}
