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

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromOrdinal(ordinal: Int) = entries.getOrElse(ordinal) { SYSTEM }
    }
}

enum class RotationMode {
    FREE, PORTRAIT, LANDSCAPE, LOCKED_PORTRAIT, LOCKED_LANDSCAPE, REVERSE_PORTRAIT
}

enum class ColorFilterMode {
    NONE, SEPIA, GRAYSCALE, NIGHT, CUSTOM
}

enum class ReaderBackground {
    BLACK, GRAY, WHITE, AUTOMATIC
}

enum class ChapterSortMode {
    BY_NUMBER_ASC, BY_NUMBER_DESC, BY_DATE_ASC, BY_DATE_DESC
}

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED, CANCELLED, PAUSED
}

data class Comic(
    val id: Long = 0,
    val title: String,
    val author: String? = null,
    val artist: String? = null,
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
    val genre: String? = null,
    val tags: String? = null,
    val status: MangaStatus = MangaStatus.UNKNOWN,
    val category: String? = null,
    val categoryIds: List<Long> = emptyList(),
    val isFavorite: Boolean = false,
    val sourceId: String? = null,
    val remoteUrl: String? = null,
    val lastChapterFetch: Instant? = null,
    val addedAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

enum class MangaStatus {
    ONGOING, COMPLETED, HIATUS, CANCELLED, UNKNOWN
}

data class Chapter(
    val id: Long = 0,
    val comicId: Long,
    val title: String,
    val number: Float,
    val pageCount: Int = 0,
    val filePath: String? = null,
    val url: String? = null,
    val scanlator: String? = null,
    val isDownloaded: Boolean = false,
    val isRead: Boolean = false,
    val isBookmarked: Boolean = false,
    val lastPageRead: Int = 0,
    val dateUpload: Long = 0,
    val dateFetch: Long = 0,
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
    val thumbnailPath: String? = null,
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
    val progress: Float = 0f,
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val retryCount: Int = 0,
    val priority: Int = 0
)

data class Category(
    val id: Long = 0,
    val name: String,
    val order: Int = 0,
    val createdAt: Instant = Instant.now()
)

data class TrackingEntry(
    val id: Long = 0,
    val comicId: Long,
    val trackerId: String,
    val remoteId: String,
    val title: String,
    val lastChapterRead: Float = 0f,
    val totalChapters: Int = 0,
    val score: Float = 0f,
    val status: TrackingStatus = TrackingStatus.READING,
    val startDate: String? = null,
    val finishDate: String? = null
)

enum class TrackingStatus {
    READING, COMPLETED, ON_HOLD, DROPPED, PLAN_TO_READ
}

data class ReadingStatistics(
    val totalChaptersRead: Int = 0,
    val totalReadingTimeMinutes: Long = 0,
    val mangaCompleted: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val averageChaptersPerDay: Float = 0f,
    val genreDistribution: Map<String, Int> = emptyMap()
)

data class BackupData(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val comics: List<Comic> = emptyList(),
    val categories: List<Category> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val trackingEntries: List<TrackingEntry> = emptyList()
)
