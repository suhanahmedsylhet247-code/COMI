package com.comi.reader.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.local.dao.HistoryRow
import com.comi.reader.data.local.entity.BookmarkEntity
import com.comi.reader.data.local.entity.ChapterEntity
import com.comi.reader.data.local.entity.ComicEntity
import com.comi.reader.data.local.entity.ReadingProgressEntity
import com.comi.reader.data.local.entity.SearchHistoryEntity
import com.comi.reader.data.parser.ComicParser
import com.comi.reader.data.preferences.AppPreferences
import com.comi.reader.domain.model.Bookmark
import com.comi.reader.domain.model.Chapter
import com.comi.reader.domain.model.Comic
import com.comi.reader.domain.model.ComicFormat
import com.comi.reader.domain.model.HistoryEntry
import com.comi.reader.domain.model.MangaStatus
import com.comi.reader.domain.model.ReadingProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComicRepository @Inject constructor(
    private val dao: ComicDao,
    private val parser: ComicParser,
    private val preferences: AppPreferences,
    @ApplicationContext private val context: Context
) {

    fun getAllComics(): Flow<List<Comic>> = dao.getAllComics().map { list ->
        list.map { it.toDomain() }
    }

    fun getFavoriteComics(): Flow<List<Comic>> = dao.getFavoriteComics().map { list ->
        list.map { it.toDomain() }
    }

    fun searchComics(query: String): Flow<List<Comic>> = dao.searchComics(query).map { list ->
        list.map { it.toDomain() }
    }

    fun getComicsByCategory(category: String): Flow<List<Comic>> =
        dao.getComicsByCategory(category).map { list -> list.map { it.toDomain() } }

    fun getComicsByStatus(status: String): Flow<List<Comic>> =
        dao.getComicsByStatus(status).map { list -> list.map { it.toDomain() } }

    fun getComicsByGenre(genre: String): Flow<List<Comic>> =
        dao.getComicsByGenre(genre).map { list -> list.map { it.toDomain() } }

    fun getCategories(): Flow<List<String>> = dao.getCategories()

    fun getGenres(): Flow<List<String>> = dao.getGenres()

    fun getComicCount(): Flow<Int> = dao.getComicCount()
    fun getFavoriteCount(): Flow<Int> = dao.getFavoriteCount()
    fun getCompletedCount(): Flow<Int> = dao.getCompletedCount()

    suspend fun getComic(id: Long): Comic? = dao.getComicById(id)?.toDomain()

    suspend fun importComic(uri: Uri): Comic = withContext(Dispatchers.IO) {
        val fileName = getFileName(uri) ?: "comic_${UUID.randomUUID()}"
        val ext = fileName.substringAfterLast('.', "cbz")
        val destFile = File(getComicsDir(), "${UUID.randomUUID()}.$ext")
        destFile.parentFile?.mkdirs()

        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Cannot open file")

        val format = parser.detectFormat(destFile)
        val pageCount = parser.getPageCount(destFile)
        val metadata = parser.extractMetadata(destFile)

        val coverDir = File(context.filesDir, "covers")
        coverDir.mkdirs()
        val coverFile = File(coverDir, "${UUID.randomUUID()}.jpg")
        val coverPath = parser.extractCoverThumbnail(destFile)?.let { thumb ->
            if (parser.saveBitmapToFile(thumb, coverFile)) coverFile.absolutePath else null
        }

        val title = metadata["title"] ?: fileName.substringBeforeLast('.')

        val entity = ComicEntity(
            title = title,
            author = metadata["author"],
            artist = metadata["artist"],
            description = metadata["description"],
            coverPath = coverPath,
            filePath = destFile.absolutePath,
            format = format.name,
            fileSize = destFile.length(),
            pageCount = pageCount,
            series = metadata["series"],
            volume = metadata["volume"]?.toIntOrNull(),
            publisher = metadata["publisher"],
            year = metadata["year"]?.toIntOrNull(),
            genre = metadata["genre"],
            tags = metadata["tags"]
        )

        val id = dao.insertComic(entity)

        dao.insertChapter(
            ChapterEntity(
                comicId = id,
                title = "Chapter 1",
                number = 1f,
                pageCount = pageCount,
                filePath = destFile.absolutePath,
                isDownloaded = true
            )
        )

        entity.copy(id = id).toDomain()
    }

    suspend fun importFromFile(file: File): Comic = withContext(Dispatchers.IO) {
        val format = parser.detectFormat(file)
        val pageCount = parser.getPageCount(file)
        val metadata = parser.extractMetadata(file)

        if (dao.getComicByPath(file.absolutePath) != null) {
            throw IllegalStateException("Comic already exists in library")
        }

        val coverDir = File(context.filesDir, "covers")
        coverDir.mkdirs()
        val coverFile = File(coverDir, "${UUID.randomUUID()}.jpg")
        val coverPath = parser.extractCoverThumbnail(file)?.let { thumb ->
            if (parser.saveBitmapToFile(thumb, coverFile)) coverFile.absolutePath else null
        }

        val title = metadata["title"] ?: file.nameWithoutExtension

        val entity = ComicEntity(
            title = title,
            author = metadata["author"],
            artist = metadata["artist"],
            description = metadata["description"],
            coverPath = coverPath,
            filePath = file.absolutePath,
            format = format.name,
            fileSize = file.length(),
            pageCount = pageCount,
            series = metadata["series"],
            volume = metadata["volume"]?.toIntOrNull(),
            publisher = metadata["publisher"],
            year = metadata["year"]?.toIntOrNull(),
            genre = metadata["genre"],
            tags = metadata["tags"]
        )

        val id = dao.insertComic(entity)
        dao.insertChapter(
            ChapterEntity(
                comicId = id,
                title = "Chapter 1",
                number = 1f,
                pageCount = pageCount,
                filePath = file.absolutePath,
                isDownloaded = true
            )
        )

        entity.copy(id = id).toDomain()
    }

    suspend fun updateComic(comic: Comic) {
        dao.getComicById(comic.id)?.let { existing ->
            dao.updateComic(
                existing.copy(
                    title = comic.title,
                    author = comic.author,
                    artist = comic.artist,
                    description = comic.description,
                    genre = comic.genre,
                    tags = comic.tags,
                    status = comic.status.name,
                    category = comic.category,
                    isFavorite = comic.isFavorite,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun toggleFavorite(comicId: Long) {
        dao.getComicById(comicId)?.let { comic ->
            dao.updateComic(comic.copy(isFavorite = !comic.isFavorite))
        }
    }

    suspend fun deleteComic(id: Long, deleteFile: Boolean = false) {
        dao.getComicById(id)?.let { comic ->
            if (deleteFile) {
                File(comic.filePath).delete()
                comic.coverPath?.let { File(it).delete() }
            }
            dao.deleteComicById(id)
        }
    }

    // Page extraction
    fun extractPage(file: File, pageIndex: Int): Bitmap? = parser.extractPage(file, pageIndex)

    // Reading progress
    suspend fun getProgress(comicId: Long): ReadingProgress? {
        return dao.getProgress(comicId)?.toDomain()
    }

    suspend fun updateProgress(comicId: Long, currentPage: Int, totalPages: Int) {
        val isIncognito = preferences.incognitoMode.first()
        if (isIncognito) return

        val percentage = if (totalPages > 0) (currentPage + 1).toFloat() / totalPages else 0f
        val existing = dao.getProgress(comicId)
        val now = System.currentTimeMillis()

        if (existing != null) {
            dao.updateProgress(
                existing.copy(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    percentage = percentage,
                    lastReadAt = now,
                    finishedAt = if (percentage >= 1f) now else null
                )
            )
        } else {
            dao.insertProgress(
                ReadingProgressEntity(
                    comicId = comicId,
                    currentPage = currentPage,
                    totalPages = totalPages,
                    percentage = percentage,
                    lastReadAt = now,
                    startedAt = now
                )
            )
        }

        // Update reading statistics
        val today = LocalDate.now().toString()
        val lastDate = preferences.lastReadDate.first()
        if (lastDate != today) {
            if (lastDate == LocalDate.now().minusDays(1).toString()) {
                val streak = preferences.readingStreakDays.first()
                preferences.setReadingStreak(streak + 1)
            } else if (lastDate != today) {
                preferences.setReadingStreak(1)
            }
            preferences.setLastReadDate(today)
        }
    }

    fun getReadingHistory(limit: Int = 50): Flow<List<HistoryEntry>> {
        return dao.getReadingHistory(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }

    // Bookmarks
    fun getBookmarks(comicId: Long): Flow<List<Bookmark>> {
        return dao.getBookmarksForComic(comicId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getRecentBookmarks(limit: Int = 20): Flow<List<Bookmark>> {
        return dao.getRecentBookmarks(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun addBookmark(comicId: Long, pageNumber: Int, label: String?, note: String?): Long {
        return dao.insertBookmark(
            BookmarkEntity(
                comicId = comicId,
                pageNumber = pageNumber,
                label = label,
                note = note
            )
        )
    }

    suspend fun deleteBookmark(id: Long) {
        dao.deleteBookmarkById(id)
    }

    suspend fun deleteAllBookmarksForComic(comicId: Long) {
        dao.deleteAllBookmarksForComic(comicId)
    }

    // Chapters
    fun getChapters(comicId: Long): Flow<List<Chapter>> {
        return dao.getChaptersForComic(comicId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getChapterCount(comicId: Long): Int = dao.getChapterCount(comicId)
    suspend fun getReadChapterCount(comicId: Long): Int = dao.getReadChapterCount(comicId)
    suspend fun getDownloadedChapterCount(comicId: Long): Int = dao.getDownloadedChapterCount(comicId)

    suspend fun toggleChapterRead(chapterId: Long) {
        dao.getChapterById(chapterId)?.let { chapter ->
            dao.setChapterRead(chapterId, !chapter.isRead)
            if (!chapter.isRead) {
                preferences.incrementChaptersRead()
            }
        }
    }

    suspend fun setChapterRead(chapterId: Long, isRead: Boolean) {
        dao.setChapterRead(chapterId, isRead)
        if (isRead) preferences.incrementChaptersRead()
    }

    suspend fun markAllChaptersRead(comicId: Long) {
        dao.markAllChaptersRead(comicId)
    }

    suspend fun markAllChaptersUnread(comicId: Long) {
        dao.markAllChaptersUnread(comicId)
    }

    suspend fun updateChapterProgress(chapterId: Long, lastPage: Int, isRead: Boolean) {
        dao.getChapterById(chapterId)?.let { chapter ->
            dao.updateChapter(chapter.copy(lastPageRead = lastPage, isRead = isRead))
        }
    }

    // Search history
    fun getSearchHistory(limit: Int = 10): Flow<List<String>> {
        return dao.getSearchHistory(limit).map { list -> list.map { it.query } }
    }

    suspend fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        val isIncognito = preferences.incognitoMode.first()
        if (isIncognito) return
        dao.insertSearchHistory(SearchHistoryEntity(query = query))
    }

    suspend fun clearSearchHistory() {
        dao.clearSearchHistory()
    }

    // Scan directory for comics
    suspend fun scanDirectory(directory: File): List<Comic> = withContext(Dispatchers.IO) {
        val supportedExtensions = setOf("cbz", "cbr", "pdf", "zip")
        val results = mutableListOf<Comic>()

        directory.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in supportedExtensions }
            .forEach { file ->
                try {
                    if (dao.getComicByPath(file.absolutePath) == null) {
                        results.add(importFromFile(file))
                    }
                } catch (_: Exception) { }
            }

        results
    }

    private fun getComicsDir(): File {
        val dir = File(context.filesDir, "comics")
        dir.mkdirs()
        return dir
    }

    private fun getFileName(uri: Uri): String? {
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    // Entity to domain mappers
    private fun ComicEntity.toDomain() = Comic(
        id = id,
        title = title,
        author = author,
        artist = artist,
        description = description,
        coverPath = coverPath,
        filePath = filePath,
        format = try { ComicFormat.valueOf(format) } catch (_: Exception) { ComicFormat.UNKNOWN },
        fileSize = fileSize,
        pageCount = pageCount,
        series = series,
        volume = volume,
        publisher = publisher,
        year = year,
        genre = genre,
        tags = tags,
        status = try { MangaStatus.valueOf(status) } catch (_: Exception) { MangaStatus.UNKNOWN },
        category = category,
        isFavorite = isFavorite,
        sourceId = sourceId,
        remoteUrl = remoteUrl,
        lastChapterFetch = lastChapterFetch?.let { Instant.ofEpochMilli(it) },
        addedAt = Instant.ofEpochMilli(addedAt),
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )

    private fun ChapterEntity.toDomain() = Chapter(
        id = id,
        comicId = comicId,
        title = title,
        number = number,
        pageCount = pageCount,
        filePath = filePath,
        url = url,
        scanlator = scanlator,
        isDownloaded = isDownloaded,
        isRead = isRead,
        isBookmarked = isBookmarked,
        lastPageRead = lastPageRead,
        dateUpload = dateUpload,
        dateFetch = dateFetch,
        addedAt = Instant.ofEpochMilli(addedAt)
    )

    private fun ReadingProgressEntity.toDomain() = ReadingProgress(
        id = id,
        comicId = comicId,
        chapterId = chapterId,
        currentPage = currentPage,
        totalPages = totalPages,
        percentage = percentage,
        lastReadAt = Instant.ofEpochMilli(lastReadAt),
        startedAt = Instant.ofEpochMilli(startedAt),
        finishedAt = finishedAt?.let { Instant.ofEpochMilli(it) }
    )

    private fun BookmarkEntity.toDomain() = Bookmark(
        id = id,
        comicId = comicId,
        chapterId = chapterId,
        pageNumber = pageNumber,
        label = label,
        note = note,
        thumbnailPath = thumbnailPath,
        createdAt = Instant.ofEpochMilli(createdAt)
    )

    private fun HistoryRow.toDomain() = HistoryEntry(
        comicId = comicId,
        comicTitle = comicTitle,
        coverPath = coverPath,
        currentPage = currentPage,
        totalPages = totalPages,
        percentage = percentage,
        lastReadAt = Instant.ofEpochMilli(lastReadAt)
    )
}
