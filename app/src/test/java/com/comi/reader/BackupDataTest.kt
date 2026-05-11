package com.comi.reader

import com.comi.reader.domain.model.BackupData
import com.comi.reader.domain.model.Category
import com.comi.reader.domain.model.Comic
import com.comi.reader.domain.model.ComicFormat
import com.comi.reader.domain.model.MangaStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class BackupDataTest {

    @Test
    fun `BackupData stores comics and categories`() {
        val comics = listOf(
            Comic(
                id = 1, title = "Test Comic", filePath = "/test",
                format = ComicFormat.CBZ, fileSize = 1000, pageCount = 10,
                status = MangaStatus.ONGOING, addedAt = Instant.now(), updatedAt = Instant.now()
            )
        )
        val categories = listOf(Category(id = 1, name = "Action", order = 0))
        val backup = BackupData(comics = comics, categories = categories)

        assertEquals(1, backup.comics.size)
        assertEquals(1, backup.categories.size)
        assertEquals("Test Comic", backup.comics[0].title)
        assertEquals("Action", backup.categories[0].name)
    }

    @Test
    fun `BackupData empty defaults`() {
        val backup = BackupData()
        assertTrue(backup.comics.isEmpty())
        assertTrue(backup.categories.isEmpty())
    }
}
