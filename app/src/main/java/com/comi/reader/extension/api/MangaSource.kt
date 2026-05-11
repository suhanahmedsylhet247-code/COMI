package com.comi.reader.extension.api

import com.comi.reader.extension.model.MangaChapter
import com.comi.reader.extension.model.MangaDetails
import com.comi.reader.extension.model.MangaPage

/**
 * Interface for manga source extensions (similar to Tachiyomi's Source interface).
 * Third-party extensions implement this to provide manga from online sources.
 */
interface ComiSource {
    val id: String
    val name: String
    val language: String
    val baseUrl: String

    suspend fun search(query: String, page: Int): List<MangaDetails>
    suspend fun getPopular(page: Int): List<MangaDetails>
    suspend fun getLatest(page: Int): List<MangaDetails>
    suspend fun getMangaDetails(url: String): MangaDetails
    suspend fun getChapterList(mangaUrl: String): List<MangaChapter>
    suspend fun getPageList(chapterUrl: String): List<MangaPage>
}

/**
 * Registry for managing installed manga source extensions.
 */
class SourceRegistry {
    private val sources = mutableMapOf<String, ComiSource>()

    fun register(source: ComiSource) {
        sources[source.id] = source
    }

    fun unregister(sourceId: String) {
        sources.remove(sourceId)
    }

    fun getSource(id: String): ComiSource? = sources[id]

    fun getAllSources(): List<ComiSource> = sources.values.toList()

    fun getSourcesByLanguage(language: String): List<ComiSource> =
        sources.values.filter { it.language == language }
}
