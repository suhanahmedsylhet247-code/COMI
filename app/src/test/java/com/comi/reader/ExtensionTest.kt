package com.comi.reader

import com.comi.reader.extension.model.AvailableSource
import com.comi.reader.extension.model.Extension
import com.comi.reader.extension.model.InstallStep
import com.comi.reader.extension.repo.ExtensionRepoIndex
import com.comi.reader.extension.repo.ExtensionRepoSource
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `extension available model has correct properties`() {
        val ext = Extension.Available(
            name = "MangaDex",
            pkgName = "eu.kanade.tachiyomi.extension.en.mangadex",
            versionName = "1.2.3",
            versionCode = 123,
            libVersion = 1.5,
            lang = "en",
            isNsfw = false,
            sources = listOf(
                AvailableSource(id = 1L, lang = "en", name = "MangaDex", baseUrl = "https://mangadex.org"),
            ),
            apkName = "tachiyomi-en-mangadex-v1.2.3.apk",
            iconUrl = "https://example.com/icon.png",
            repoUrl = "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json",
        )
        assertEquals("MangaDex", ext.name)
        assertEquals("en", ext.lang)
        assertFalse(ext.isNsfw)
        assertEquals(1, ext.sources.size)
        assertEquals("MangaDex", ext.sources.first().name)
    }

    @Test
    fun `install step enum has all values`() {
        assertEquals(6, InstallStep.entries.size)
        assertTrue(InstallStep.entries.contains(InstallStep.Idle))
        assertTrue(InstallStep.entries.contains(InstallStep.Downloading))
        assertTrue(InstallStep.entries.contains(InstallStep.Installing))
        assertTrue(InstallStep.entries.contains(InstallStep.Installed))
        assertTrue(InstallStep.entries.contains(InstallStep.Error))
    }

    @Test
    fun `extension repo index deserialization`() {
        val jsonStr = """[
            {
                "name": "Tachiyomi: MangaDex",
                "pkg": "eu.kanade.tachiyomi.extension.en.mangadex",
                "apk": "tachiyomi-en-mangadex-v1.2.3.apk",
                "lang": "en",
                "code": 123,
                "version": "1.2.3",
                "nsfw": 0,
                "sources": [
                    {"name": "MangaDex", "lang": "en", "id": 2499283573021220255, "baseUrl": "https://mangadex.org"}
                ]
            }
        ]"""
        val index = json.decodeFromString<List<ExtensionRepoIndex>>(jsonStr)
        assertEquals(1, index.size)
        assertEquals("Tachiyomi: MangaDex", index[0].name)
        assertEquals("eu.kanade.tachiyomi.extension.en.mangadex", index[0].pkg)
        assertEquals("en", index[0].lang)
        assertEquals(0, index[0].nsfw)
        assertEquals(1, index[0].sources.size)
        assertEquals("MangaDex", index[0].sources[0].name)
    }

    @Test
    fun `extension repo index deserialization without sources`() {
        val jsonStr = """[
            {
                "name": "Test Extension",
                "pkg": "test.pkg",
                "apk": "test.apk",
                "lang": "all",
                "code": 1,
                "version": "0.1"
            }
        ]"""
        val index = json.decodeFromString<List<ExtensionRepoIndex>>(jsonStr)
        assertEquals(1, index.size)
        assertEquals(0, index[0].sources.size)
    }

    @Test
    fun `extension repo source has correct properties`() {
        val source = ExtensionRepoSource(
            name = "MangaDex",
            lang = "en",
            id = 2499283573021220255,
            baseUrl = "https://mangadex.org",
        )
        assertEquals("MangaDex", source.name)
        assertEquals("en", source.lang)
        assertEquals(2499283573021220255, source.id)
    }
}
