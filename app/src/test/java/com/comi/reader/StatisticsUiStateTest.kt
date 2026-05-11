package com.comi.reader

import com.comi.reader.ui.statistics.StatisticsUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsUiStateTest {

    @Test
    fun `StatisticsUiState defaults to zero`() {
        val state = StatisticsUiState()
        assertEquals(0, state.chaptersRead)
        assertEquals(0L, state.readingTimeMinutes)
        assertEquals(0, state.totalComics)
        assertEquals(0, state.favorites)
        assertEquals(0, state.completed)
        assertEquals(0, state.streak)
    }

    @Test
    fun `StatisticsUiState with values`() {
        val state = StatisticsUiState(
            chaptersRead = 150,
            readingTimeMinutes = 3600,
            totalComics = 50,
            favorites = 10,
            completed = 8,
            streak = 7
        )
        assertEquals(150, state.chaptersRead)
        assertEquals(3600L, state.readingTimeMinutes)
        assertEquals(50, state.totalComics)
    }
}
