package com.comi.reader

import com.comi.reader.domain.model.TrackingEntry
import com.comi.reader.domain.model.TrackingStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingEntryTest {

    @Test
    fun `TrackingEntry creation`() {
        val entry = TrackingEntry(
            id = 1,
            comicId = 10,
            trackerId = "myanimelist",
            remoteId = "12345",
            status = TrackingStatus.READING,
            score = 8.5f,
            lastChapterRead = 42f,
            title = "One Piece"
        )

        assertEquals(1L, entry.id)
        assertEquals(10L, entry.comicId)
        assertEquals("myanimelist", entry.trackerId)
        assertEquals(TrackingStatus.READING, entry.status)
        assertEquals(8.5f, entry.score, 0.01f)
        assertEquals(42f, entry.lastChapterRead, 0.01f)
    }

    @Test
    fun `TrackingStatus has all values`() {
        val statuses = TrackingStatus.entries
        assertEquals(5, statuses.size)
        assertEquals(TrackingStatus.READING, statuses[0])
        assertEquals(TrackingStatus.COMPLETED, statuses[1])
        assertEquals(TrackingStatus.ON_HOLD, statuses[2])
        assertEquals(TrackingStatus.DROPPED, statuses[3])
        assertEquals(TrackingStatus.PLAN_TO_READ, statuses[4])
    }
}
