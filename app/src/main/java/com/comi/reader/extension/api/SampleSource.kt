package com.comi.reader.extension.api

import com.comi.reader.extension.model.MangaChapter
import com.comi.reader.extension.model.MangaDetails
import com.comi.reader.extension.model.MangaPage
import com.comi.reader.extension.model.MangaStatus

/**
 * Sample built-in source demonstrating the extension API.
 * Real sources would connect to actual manga hosting sites.
 */
class LocalSource : ComiSource {
    override val id: String = "local"
    override val name: String = "Local Storage"
    override val language: String = "all"
    override val baseUrl: String = "file://"

    override suspend fun search(query: String, page: Int): List<MangaDetails> {
        // Local source search is handled by the repository
        return emptyList()
    }

    override suspend fun getPopular(page: Int): List<MangaDetails> = emptyList()
    override suspend fun getLatest(page: Int): List<MangaDetails> = emptyList()

    override suspend fun getMangaDetails(url: String): MangaDetails {
        return MangaDetails(
            url = url,
            title = url.substringAfterLast('/').substringBeforeLast('.'),
            status = MangaStatus.UNKNOWN
        )
    }

    override suspend fun getChapterList(mangaUrl: String): List<MangaChapter> {
        return listOf(
            MangaChapter(
                url = mangaUrl,
                title = "Full Comic",
                number = 1f
            )
        )
    }

    override suspend fun getPageList(chapterUrl: String): List<MangaPage> {
        // Pages are loaded directly from the file by ComicParser
        return emptyList()
    }
}
