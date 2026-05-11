package com.comi.reader

import com.comi.reader.extension.api.LocalSource
import com.comi.reader.extension.api.SourceRegistry
import com.comi.reader.extension.model.MangaStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionTest {

    @Test
    fun `local source has correct properties`() {
        val source = LocalSource()
        assertEquals("local", source.id)
        assertEquals("Local Storage", source.name)
        assertEquals("all", source.language)
        assertEquals("file://", source.baseUrl)
    }

    @Test
    fun `local source search returns empty`() = runTest {
        val source = LocalSource()
        val results = source.search("test", 1)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `local source popular returns empty`() = runTest {
        val source = LocalSource()
        val results = source.getPopular(1)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `local source details extracts title from path`() = runTest {
        val source = LocalSource()
        val details = source.getMangaDetails("/path/to/comic.cbz")
        assertEquals("comic", details.title)
        assertEquals("/path/to/comic.cbz", details.url)
        assertEquals(MangaStatus.UNKNOWN, details.status)
    }

    @Test
    fun `local source chapter list returns single chapter`() = runTest {
        val source = LocalSource()
        val chapters = source.getChapterList("/comic.cbz")
        assertEquals(1, chapters.size)
        assertEquals("Full Comic", chapters[0].title)
        assertEquals(1f, chapters[0].number, 0.01f)
    }

    @Test
    fun `source registry register and retrieve`() {
        val registry = SourceRegistry()
        val source = LocalSource()
        registry.register(source)

        assertNotNull(registry.getSource("local"))
        assertEquals("Local Storage", registry.getSource("local")?.name)
    }

    @Test
    fun `source registry unregister`() {
        val registry = SourceRegistry()
        val source = LocalSource()
        registry.register(source)
        registry.unregister("local")

        assertNull(registry.getSource("local"))
    }

    @Test
    fun `source registry get all sources`() {
        val registry = SourceRegistry()
        registry.register(LocalSource())

        assertEquals(1, registry.getAllSources().size)
    }

    @Test
    fun `source registry get by language`() {
        val registry = SourceRegistry()
        registry.register(LocalSource())

        assertEquals(1, registry.getSourcesByLanguage("all").size)
        assertEquals(0, registry.getSourcesByLanguage("en").size)
    }

    @Test
    fun `manga status enum has all values`() {
        assertEquals(5, MangaStatus.entries.size)
        assertTrue(MangaStatus.entries.contains(MangaStatus.ONGOING))
        assertTrue(MangaStatus.entries.contains(MangaStatus.COMPLETED))
    }
}
