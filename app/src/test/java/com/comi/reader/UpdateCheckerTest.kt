package com.comi.reader

import com.comi.reader.data.local.entity.ChapterEntity
import com.comi.reader.data.local.entity.ComicEntity
import com.comi.reader.data.local.entity.MangaGroupEntity
import com.comi.reader.domain.dedup.ChapterDeduplicator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun `grouped comics deduplicate new chapters for notifications`() {
        // Comic A and Comic B are the same manga from different sources
        val comicA = ComicEntity(id = 1, title = "Solo Leveling", sourceId = "source1", remoteUrl = "/solo-leveling")
        val comicB = ComicEntity(id = 2, title = "Solo Leveling", sourceId = "source2", remoteUrl = "/solo-leveling-2")

        val groupA = MangaGroupEntity(comicId = 1, groupId = 100)
        val groupB = MangaGroupEntity(comicId = 2, groupId = 100)

        // Both sources report new chapter 50
        val chaptersA = listOf(
            ChapterEntity(comicId = 1, title = "Chapter 50", number = 50f),
        )
        val chaptersB = listOf(
            ChapterEntity(comicId = 2, title = "Ch 50", number = 50f),
        )

        // Deduplication should yield only 1 unique chapter
        val newChaptersBySource = mapOf("source1" to chaptersA, "source2" to chaptersB)
        val deduped = ChapterDeduplicator.deduplicateNewChapters(newChaptersBySource)
        assertEquals(1, deduped.size)
        assertEquals(50f, deduped[0].number, 0.01f)
    }

    @Test
    fun `ungrouped comics get separate notifications`() {
        // Two different manga
        val chaptersA = listOf(
            ChapterEntity(comicId = 1, title = "Chapter 10", number = 10f),
        )
        val chaptersB = listOf(
            ChapterEntity(comicId = 2, title = "Chapter 5", number = 5f),
        )

        // These are separate manga, so no dedup
        val dedupA = ChapterDeduplicator.deduplicateNewChapters(mapOf("s1" to chaptersA))
        val dedupB = ChapterDeduplicator.deduplicateNewChapters(mapOf("s2" to chaptersB))
        assertEquals(1, dedupA.size)
        assertEquals(1, dedupB.size)
    }

    @Test
    fun `filler chapters excluded from notifications`() {
        val chapters = listOf(
            ChapterEntity(comicId = 1, title = "Chapter 10", number = 10f),
            ChapterEntity(comicId = 1, title = "Chapter 10.5", number = 10.5f),
            ChapterEntity(comicId = 1, title = "Chapter 11", number = 11f),
        )

        val nonFillers = chapters.filter { !ChapterDeduplicator.isFillerChapter(it.number) }
        assertEquals(2, nonFillers.size)
        assertTrue(nonFillers.all { it.number == 10f || it.number == 11f })
    }

    @Test
    fun `same chapter from 3 sources produces 1 notification`() {
        val chaptersSource1 = listOf(ChapterEntity(comicId = 1, title = "Chapter 20", number = 20f))
        val chaptersSource2 = listOf(ChapterEntity(comicId = 2, title = "Ch 20", number = 20f))
        val chaptersSource3 = listOf(ChapterEntity(comicId = 3, title = "Chapter Twenty", number = 20f))

        val deduped = ChapterDeduplicator.deduplicateNewChapters(
            mapOf("s1" to chaptersSource1, "s2" to chaptersSource2, "s3" to chaptersSource3)
        )

        assertEquals(1, deduped.size)
    }

    @Test
    fun `different chapters from same source are not deduped`() {
        val chapters = listOf(
            ChapterEntity(comicId = 1, title = "Chapter 10", number = 10f),
            ChapterEntity(comicId = 1, title = "Chapter 11", number = 11f),
            ChapterEntity(comicId = 1, title = "Chapter 12", number = 12f),
        )

        val deduped = ChapterDeduplicator.deduplicateNewChapters(mapOf("s1" to chapters))
        assertEquals(3, deduped.size)
    }
}
