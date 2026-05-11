package com.comi.reader

import com.comi.reader.domain.model.Chapter
import com.comi.reader.domain.model.ChapterSortMode
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ChapterSortModeTest {

    private val chapters = listOf(
        Chapter(id = 1, comicId = 1, title = "Ch 1", number = 1f, pageCount = 20, filePath = "/1", addedAt = Instant.ofEpochMilli(1000)),
        Chapter(id = 2, comicId = 1, title = "Ch 3", number = 3f, pageCount = 22, filePath = "/3", addedAt = Instant.ofEpochMilli(3000)),
        Chapter(id = 3, comicId = 1, title = "Ch 2", number = 2f, pageCount = 18, filePath = "/2", addedAt = Instant.ofEpochMilli(2000))
    )

    @Test
    fun `sort by number ascending`() {
        val sorted = chapters.sortedBy { it.number }
        assertEquals(1f, sorted[0].number, 0.01f)
        assertEquals(2f, sorted[1].number, 0.01f)
        assertEquals(3f, sorted[2].number, 0.01f)
    }

    @Test
    fun `sort by number descending`() {
        val sorted = chapters.sortedByDescending { it.number }
        assertEquals(3f, sorted[0].number, 0.01f)
        assertEquals(2f, sorted[1].number, 0.01f)
        assertEquals(1f, sorted[2].number, 0.01f)
    }

    @Test
    fun `ChapterSortMode has all values`() {
        assertEquals(4, ChapterSortMode.entries.size)
    }
}
