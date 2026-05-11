package com.comi.reader

import com.comi.reader.domain.model.Bookmark
import com.comi.reader.domain.model.Comic
import com.comi.reader.domain.model.ComicFormat
import com.comi.reader.domain.model.DownloadStatus
import com.comi.reader.domain.model.DownloadTask
import com.comi.reader.domain.model.HistoryEntry
import com.comi.reader.domain.model.ReadingMode
import com.comi.reader.domain.model.ReadingProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ModelsTest {

    @Test
    fun `comic default values`() {
        val comic = Comic(
            title = "Test Comic",
            filePath = "/path/to/comic.cbz",
            format = ComicFormat.CBZ
        )
        assertEquals(0L, comic.id)
        assertEquals("Test Comic", comic.title)
        assertNull(comic.author)
        assertNull(comic.description)
        assertNull(comic.coverPath)
        assertEquals(ComicFormat.CBZ, comic.format)
        assertEquals(0, comic.pageCount)
        assertFalse(comic.isFavorite)
    }

    @Test
    fun `comic with all fields`() {
        val comic = Comic(
            id = 1,
            title = "Batman #1",
            author = "Bob Kane",
            description = "First issue",
            coverPath = "/covers/batman.jpg",
            filePath = "/comics/batman.cbz",
            format = ComicFormat.CBZ,
            fileSize = 50_000_000,
            pageCount = 32,
            series = "Batman",
            volume = 1,
            publisher = "DC Comics",
            year = 1940,
            category = "Superhero",
            isFavorite = true
        )
        assertEquals(1L, comic.id)
        assertEquals("Batman #1", comic.title)
        assertEquals("Bob Kane", comic.author)
        assertEquals(32, comic.pageCount)
        assertTrue(comic.isFavorite)
        assertEquals("Batman", comic.series)
    }

    @Test
    fun `reading progress percentage`() {
        val progress = ReadingProgress(
            comicId = 1,
            currentPage = 15,
            totalPages = 30,
            percentage = 0.5f
        )
        assertEquals(0.5f, progress.percentage, 0.01f)
        assertNull(progress.finishedAt)
    }

    @Test
    fun `reading progress finished`() {
        val now = Instant.now()
        val progress = ReadingProgress(
            comicId = 1,
            currentPage = 29,
            totalPages = 30,
            percentage = 1.0f,
            finishedAt = now
        )
        assertEquals(1.0f, progress.percentage, 0.01f)
        assertEquals(now, progress.finishedAt)
    }

    @Test
    fun `bookmark creation`() {
        val bookmark = Bookmark(
            comicId = 1,
            pageNumber = 10,
            label = "Cool page",
            note = "Great splash page"
        )
        assertEquals(10, bookmark.pageNumber)
        assertEquals("Cool page", bookmark.label)
    }

    @Test
    fun `reading modes`() {
        assertEquals(4, ReadingMode.entries.size)
        assertTrue(ReadingMode.entries.contains(ReadingMode.LEFT_TO_RIGHT))
        assertTrue(ReadingMode.entries.contains(ReadingMode.RIGHT_TO_LEFT))
        assertTrue(ReadingMode.entries.contains(ReadingMode.VERTICAL))
        assertTrue(ReadingMode.entries.contains(ReadingMode.WEBTOON))
    }

    @Test
    fun `comic format enum`() {
        assertEquals(4, ComicFormat.entries.size)
        assertTrue(ComicFormat.entries.contains(ComicFormat.CBZ))
        assertTrue(ComicFormat.entries.contains(ComicFormat.CBR))
        assertTrue(ComicFormat.entries.contains(ComicFormat.PDF))
        assertTrue(ComicFormat.entries.contains(ComicFormat.UNKNOWN))
    }

    @Test
    fun `download status states`() {
        assertEquals(5, DownloadStatus.entries.size)
        val task = DownloadTask(
            comicId = 1,
            chapterId = 1,
            chapterTitle = "Chapter 1",
            status = DownloadStatus.PENDING
        )
        assertEquals(DownloadStatus.PENDING, task.status)
        assertEquals(0f, task.progress, 0.01f)
    }

    @Test
    fun `history entry`() {
        val now = Instant.now()
        val entry = HistoryEntry(
            comicId = 1,
            comicTitle = "Test",
            coverPath = null,
            currentPage = 5,
            totalPages = 20,
            percentage = 0.25f,
            lastReadAt = now
        )
        assertEquals(5, entry.currentPage)
        assertEquals(0.25f, entry.percentage, 0.01f)
    }
}
