package com.comi.reader

import com.comi.reader.ui.downloads.DownloadUiItem
import com.comi.reader.ui.downloads.DownloadsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadUiItemTest {

    @Test
    fun `DownloadUiItem creation`() {
        val item = DownloadUiItem(
            id = 1,
            comicId = 10,
            chapterId = 100,
            chapterTitle = "Chapter 1",
            status = "DOWNLOADING",
            progress = 0.5f,
            totalBytes = 10000,
            downloadedBytes = 5000
        )
        assertEquals(1L, item.id)
        assertEquals("DOWNLOADING", item.status)
        assertEquals(0.5f, item.progress, 0.01f)
    }

    @Test
    fun `DownloadsUiState empty by default`() {
        val state = DownloadsUiState()
        assertTrue(state.tasks.isEmpty())
    }
}
