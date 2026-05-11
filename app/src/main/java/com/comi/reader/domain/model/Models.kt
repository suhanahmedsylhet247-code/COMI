package com.comi.reader.domain.model

import java.time.Instant

enum class ReadingMode {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL,
    WEBTOON
}

enum class ComicFormat {
    CBZ, CBR, PDF, UNKNOWN
}

data class Comic(
    val id: Long = 0,
    val title: String,
    val author: String? = null,
    val description: String? = null,
    val coverPath: String? = null,
    val filePath: String,
    val format: ComicFormat,
    val fileSize: Long = 0,
    val pageCount: Int = 0,
    val series: String? = null,
    val volume: Int? = null,
    val publisher: String? = null,
    val year: Int? = null,
    val category: String? = null,
    val isFavorite: Boolean = false,
    val addedAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

data class Chapter(
    val id: Long = 0,
    val comicId: Long,
    val title: String,
    val number: Float,
    val pageCount: Int = 0,
    val filePath: String? = null,
    val url: String? = null,
    val isDownloaded: Boolean = false,
    val isRead: Boolean = false,
    val lastPageRead: Int = 0,
    val addedAt: Instant = Instant.now()
)

data class ReadingProgress(
    val id: Long = 0,
    val comicId: Long,
    val chapterId: Long? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val percentage: Float = 0f,
    val lastReadAt: Instant = Instant.now(),
    val startedAt: Instant = Instant.now(),
    val finishedAt: Instant? = null
)

data class Bookmark(
    val id: Long = 0,
    val comicId: Long,
    val chapterId: Long? = null,
    val pageNumber: Int,
    val label: String? = null,
    val note: String? = null,
    val createdAt: Instant = Instant.now()
)

data class HistoryEntry(
    val comicId: Long,
    val comicTitle: String,
    val coverPath: String?,
    val currentPage: Int,
    val totalPages: Int,
    val percentage: Float,
    val lastReadAt: Instant
)

data class MangaSource(
    val id: String,
    val name: String,
    val language: String,
    val baseUrl: String,
    val isEnabled: Boolean = true
)

data class MangaSearchResult(
    val sourceId: String,
    val title: String,
    val url: String,
    val coverUrl: String?,
    val author: String? = null,
    val description: String? = null
)

data class DownloadTask(
    val id: Long = 0,
    val comicId: Long,
    val chapterId: Long,
    val chapterTitle: String,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Float = 0f
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED, CANCELLED
}
