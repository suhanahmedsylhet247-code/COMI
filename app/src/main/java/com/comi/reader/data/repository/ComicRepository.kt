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
import com.comi.reader.data.parser.ComicParser
import com.comi.reader.domain.model.Bookmark
import com.comi.reader.domain.model.Chapter
import com.comi.reader.domain.model.Comic
import com.comi.reader.domain.model.ComicFormat
import com.comi.reader.domain.model.HistoryEntry
import com.comi.reader.domain.model.ReadingProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComicRepository @Inject constructor(
    private val dao: ComicDao,
    private val parser: ComicParser,
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

    fun getCategories(): Flow<List<String>> = dao.getCategories()

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
            description = metadata["description"],
            coverPath = coverPath,
            filePath = destFile.absolutePath,
            format = format.name,
            fileSize = destFile.length(),
            pageCount = pageCount,
            series = metadata["series"],
            volume = metadata["volume"]?.toIntOrNull(),
            publisher = metadata["publisher"],
            year = metadata["year"]?.toIntOrNull()
        )

        val id = dao.insertComic(entity)

        // Create a single chapter for local files
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
            description = metadata["description"],
            coverPath = coverPath,
            filePath = file.absolutePath,
            format = format.name,
            fileSize = file.length(),
            pageCount = pageCount,
            series = metadata["series"],
            volume = metadata["volume"]?.toIntOrNull(),
            publisher = metadata["publisher"],
            year = metadata["year"]?.toIntOrNull()
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
                    description = comic.description,
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
    }

    fun getReadingHistory(limit: Int = 20): Flow<List<HistoryEntry>> {
        return dao.getReadingHistory(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    // Bookmarks
    fun getBookmarks(comicId: Long): Flow<List<Bookmark>> {
        return dao.getBookmarksForComic(comicId).map { list ->
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

    // Chapters
    fun getChapters(comicId: Long): Flow<List<Chapter>> {
        return dao.getChaptersForComic(comicId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun updateChapterProgress(chapterId: Long, lastPage: Int, isRead: Boolean) {
        dao.getChapterById(chapterId)?.let { chapter ->
            dao.updateChapter(chapter.copy(lastPageRead = lastPage, isRead = isRead))
        }
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
                } catch (_: Exception) {
                    // Skip files that can't be imported
                }
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
        category = category,
        isFavorite = isFavorite,
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
        isDownloaded = isDownloaded,
        isRead = isRead,
        lastPageRead = lastPageRead,
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
