package com.comi.reader

import com.comi.reader.domain.model.ReadingStatistics
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingStatisticsTest {

    @Test
    fun `ReadingStatistics defaults to zero`() {
        val stats = ReadingStatistics()
        assertEquals(0, stats.totalChaptersRead)
        assertEquals(0L, stats.totalReadingTimeMinutes)
        assertEquals(0, stats.mangaCompleted)
        assertEquals(0, stats.currentStreak)
    }

    @Test
    fun `ReadingStatistics with values`() {
        val stats = ReadingStatistics(
            totalChaptersRead = 100,
            totalReadingTimeMinutes = 500,
            mangaCompleted = 5,
            currentStreak = 30
        )
        assertEquals(100, stats.totalChaptersRead)
        assertEquals(500L, stats.totalReadingTimeMinutes)
        assertEquals(5, stats.mangaCompleted)
        assertEquals(30, stats.currentStreak)
    }
}
