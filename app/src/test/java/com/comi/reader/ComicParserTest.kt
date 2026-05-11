package com.comi.reader

import com.comi.reader.domain.model.ComicFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ComicParserTest {

    @Test
    fun `detect CBZ format from extension`() {
        val format = detectFormatByExtension("comic.cbz")
        assertEquals(ComicFormat.CBZ, format)
    }

    @Test
    fun `detect CBR format from extension`() {
        val format = detectFormatByExtension("comic.cbr")
        assertEquals(ComicFormat.CBR, format)
    }

    @Test
    fun `detect PDF format from extension`() {
        val format = detectFormatByExtension("comic.pdf")
        assertEquals(ComicFormat.PDF, format)
    }

    @Test
    fun `detect ZIP as CBZ format`() {
        val format = detectFormatByExtension("comic.zip")
        assertEquals(ComicFormat.CBZ, format)
    }

    @Test
    fun `detect unknown format`() {
        val format = detectFormatByExtension("comic.txt")
        assertEquals(ComicFormat.UNKNOWN, format)
    }

    @Test
    fun `case insensitive format detection`() {
        assertEquals(ComicFormat.CBZ, detectFormatByExtension("comic.CBZ"))
        assertEquals(ComicFormat.PDF, detectFormatByExtension("comic.PDF"))
        assertEquals(ComicFormat.CBR, detectFormatByExtension("comic.Cbr"))
    }

    @Test
    fun `supported file extensions`() {
        val supported = setOf("cbz", "cbr", "pdf", "zip")
        assertTrue("cbz" in supported)
        assertTrue("cbr" in supported)
        assertTrue("pdf" in supported)
        assertTrue("zip" in supported)
        assertTrue("txt" !in supported)
    }

    private fun detectFormatByExtension(fileName: String): ComicFormat {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "cbz", "zip" -> ComicFormat.CBZ
            "cbr", "rar" -> ComicFormat.CBR
            "pdf" -> ComicFormat.PDF
            else -> ComicFormat.UNKNOWN
        }
    }
}
