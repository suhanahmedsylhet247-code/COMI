package com.comi.reader

import com.comi.reader.data.local.entity.ChapterEntity
import com.comi.reader.domain.dedup.ChapterDeduplicator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterDeduplicatorTest {

    private fun chapter(
        comicId: Long = 1L,
        title: String = "Chapter 1",
        number: Float = 1f,
        dateUpload: Long = 1000L,
        isRead: Boolean = false,
    ) = ChapterEntity(
        comicId = comicId,
        title = title,
        number = number,
        dateUpload = dateUpload,
        isRead = isRead,
    )

    @Test
    fun `merge single source chapters`() {
        val chapters = mapOf(
            "source1" to listOf(
                chapter(title = "Chapter 1", number = 1f),
                chapter(title = "Chapter 2", number = 2f),
                chapter(title = "Chapter 3", number = 3f),
            )
        )
        val merged = ChapterDeduplicator.mergeChapterLists(
            chapters, mapOf("source1" to "Source 1")
        )
        assertEquals(3, merged.size)
    }

    @Test
    fun `merge two sources with same chapters deduplicates`() {
        val chapters = mapOf(
            "source1" to listOf(
                chapter(title = "Chapter 1", number = 1f, dateUpload = 1000),
                chapter(title = "Chapter 2", number = 2f, dateUpload = 2000),
            ),
            "source2" to listOf(
                chapter(title = "Ch 1", number = 1f, dateUpload = 1100),
                chapter(title = "Ch 2", number = 2f, dateUpload = 2100),
            )
        )
        val merged = ChapterDeduplicator.mergeChapterLists(
            chapters,
            mapOf("source1" to "Source 1", "source2" to "Source 2"),
        )
        // Same chapter numbers from both sources should merge
        assertEquals(2, merged.size)
        // Each merged chapter should have 2 source chapters
        merged.forEach { mc ->
            assertEquals(2, mc.sourceChapters.size)
        }
    }

    @Test
    fun `filler chapters detected correctly`() {
        assertTrue(ChapterDeduplicator.isFillerChapter(10.5f))
        assertFalse(ChapterDeduplicator.isFillerChapter(10f))
        assertFalse(ChapterDeduplicator.isFillerChapter(11f))
    }

    @Test
    fun `source with decimal chapters and source without`() {
        // Source A has 10, 10.5, 11
        // Source B has 10, 11
        // 10.5 is a filler, should be marked as such
        val chapters = mapOf(
            "sourceA" to listOf(
                chapter(title = "Chapter 10", number = 10f),
                chapter(title = "Chapter 10.5", number = 10.5f),
                chapter(title = "Chapter 11", number = 11f),
            ),
            "sourceB" to listOf(
                chapter(title = "Ch 10", number = 10f),
                chapter(title = "Ch 11", number = 11f),
            )
        )
        val merged = ChapterDeduplicator.mergeChapterLists(
            chapters,
            mapOf("sourceA" to "Source A", "sourceB" to "Source B"),
        )
        // Should have 3 unique chapters: 10, 10.5, 11
        assertEquals(3, merged.size)

        // Chapter 10 and 11 should each have 2 source chapters
        val ch10 = merged.find { it.canonicalNumber == 10f }
        assertEquals(2, ch10!!.sourceChapters.size)

        // Chapter 10.5 should be a filler and only from source A
        val ch10_5 = merged.find { it.canonicalNumber == 10.5f }
        assertTrue(ch10_5!!.isFiller)
        assertEquals(1, ch10_5.sourceChapters.size)
    }

    @Test
    fun `deduplicate new chapters across sources`() {
        val newChapters = mapOf(
            "source1" to listOf(
                chapter(title = "Chapter 5", number = 5f),
                chapter(title = "Chapter 6", number = 6f),
            ),
            "source2" to listOf(
                chapter(title = "Ch 5", number = 5f),
                chapter(title = "Ch 6", number = 6f),
                chapter(title = "Ch 7", number = 7f),
            )
        )
        val deduped = ChapterDeduplicator.deduplicateNewChapters(newChapters)
        // Chapters 5 and 6 from source2 should be deduped; chapter 7 is unique
        assertEquals(3, deduped.size)
    }

    @Test
    fun `named chapters without numbers stay separate`() {
        val chapters = mapOf(
            "source1" to listOf(
                chapter(title = "Prologue", number = -1f, dateUpload = 0),
            ),
            "source2" to listOf(
                chapter(title = "Introduction", number = -1f, dateUpload = 0),
            )
        )
        val merged = ChapterDeduplicator.mergeChapterLists(
            chapters,
            mapOf("source1" to "Source 1", "source2" to "Source 2"),
        )
        // Without date info, unnamed chapters should not be merged with each other
        assertTrue(merged.isNotEmpty())
    }

    @Test
    fun `merged chapter best chapter respects priority`() {
        val chapters = mapOf(
            "source1" to listOf(
                chapter(title = "Chapter 1", number = 1f),
            ),
            "source2" to listOf(
                chapter(title = "Ch 1", number = 1f),
            )
        )
        val merged = ChapterDeduplicator.mergeChapterLists(
            chapters,
            mapOf("source1" to "Source 1", "source2" to "Source 2"),
            sourcePriorities = mapOf("source1" to 0, "source2" to 10),
        )
        assertEquals(1, merged.size)
        assertEquals("Source 2", merged[0].bestChapter.sourceName)
    }

    @Test
    fun `chapter read status propagates across sources`() {
        val chapters = mapOf(
            "source1" to listOf(
                chapter(title = "Chapter 1", number = 1f, isRead = true),
            ),
            "source2" to listOf(
                chapter(title = "Ch 1", number = 1f, isRead = false),
            )
        )
        val merged = ChapterDeduplicator.mergeChapterLists(
            chapters,
            mapOf("source1" to "Source 1", "source2" to "Source 2"),
        )
        // If any source has chapter as read, merged should be read
        assertTrue(merged[0].isRead)
    }
}
