package com.comi.reader

import com.comi.reader.util.FileUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileUtilsTest {

    @Test
    fun `format small file size`() {
        assertEquals("500 B", FileUtils.formatFileSize(500))
    }

    @Test
    fun `format KB file size`() {
        val result = FileUtils.formatFileSize(2048)
        assertTrue(result.contains("KB"))
    }

    @Test
    fun `format MB file size`() {
        val result = FileUtils.formatFileSize(5_242_880)
        assertTrue(result.contains("MB"))
    }

    @Test
    fun `format GB file size`() {
        val result = FileUtils.formatFileSize(2_147_483_648)
        assertTrue(result.contains("GB"))
    }

    @Test
    fun `supported format CBZ`() {
        assertTrue(FileUtils.isSupportedFormat("comic.cbz"))
    }

    @Test
    fun `supported format CBR`() {
        assertTrue(FileUtils.isSupportedFormat("comic.cbr"))
    }

    @Test
    fun `supported format PDF`() {
        assertTrue(FileUtils.isSupportedFormat("comic.pdf"))
    }

    @Test
    fun `supported format ZIP`() {
        assertTrue(FileUtils.isSupportedFormat("comic.zip"))
    }

    @Test
    fun `unsupported format TXT`() {
        assertFalse(FileUtils.isSupportedFormat("readme.txt"))
    }

    @Test
    fun `unsupported format JPG`() {
        assertFalse(FileUtils.isSupportedFormat("image.jpg"))
    }

    @Test
    fun `no extension is unsupported`() {
        assertFalse(FileUtils.isSupportedFormat("noextension"))
    }
}
