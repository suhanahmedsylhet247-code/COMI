package com.comi.reader

import com.comi.reader.domain.model.MangaStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class MangaStatusTest {

    @Test
    fun `MangaStatus has all expected values`() {
        val statuses = MangaStatus.entries
        assertEquals(5, statuses.size)
    }

    @Test
    fun `MangaStatus valueOf works`() {
        assertEquals(MangaStatus.ONGOING, MangaStatus.valueOf("ONGOING"))
        assertEquals(MangaStatus.COMPLETED, MangaStatus.valueOf("COMPLETED"))
        assertEquals(MangaStatus.HIATUS, MangaStatus.valueOf("HIATUS"))
        assertEquals(MangaStatus.CANCELLED, MangaStatus.valueOf("CANCELLED"))
        assertEquals(MangaStatus.UNKNOWN, MangaStatus.valueOf("UNKNOWN"))
    }
}
