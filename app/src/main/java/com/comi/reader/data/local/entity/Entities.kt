package com.comi.reader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "comics")
data class ComicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String? = null,
    val artist: String? = null,
    val description: String? = null,
    @ColumnInfo(name = "cover_path") val coverPath: String? = null,
    @ColumnInfo(name = "file_path") val filePath: String = "",
    val format: String = "SOURCE",
    @ColumnInfo(name = "file_size") val fileSize: Long = 0,
    @ColumnInfo(name = "page_count") val pageCount: Int = 0,
    val series: String? = null,
    val volume: Int? = null,
    val publisher: String? = null,
    val year: Int? = null,
    val genre: String? = null,
    val tags: String? = null,
    val status: String = "UNKNOWN",
    val category: String? = null,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "source_id") val sourceId: String? = null,
    @ColumnInfo(name = "remote_url") val remoteUrl: String? = null,
    @ColumnInfo(name = "last_chapter_fetch") val lastChapterFetch: Long? = null,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chapters",
    foreignKeys = [ForeignKey(
        entity = ComicEntity::class,
        parentColumns = ["id"],
        childColumns = ["comic_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("comic_id")]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "comic_id") val comicId: Long,
    val title: String,
    val number: Float,
    @ColumnInfo(name = "page_count") val pageCount: Int = 0,
    @ColumnInfo(name = "file_path") val filePath: String? = null,
    val url: String? = null,
    val scanlator: String? = null,
    @ColumnInfo(name = "is_downloaded") val isDownloaded: Boolean = false,
    @ColumnInfo(name = "is_read") val isRead: Boolean = false,
    @ColumnInfo(name = "is_bookmarked") val isBookmarked: Boolean = false,
    @ColumnInfo(name = "last_page_read") val lastPageRead: Int = 0,
    @ColumnInfo(name = "date_upload") val dateUpload: Long = 0,
    @ColumnInfo(name = "date_fetch") val dateFetch: Long = 0,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reading_progress",
    foreignKeys = [ForeignKey(
        entity = ComicEntity::class,
        parentColumns = ["id"],
        childColumns = ["comic_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("comic_id", unique = true)]
)
data class ReadingProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "comic_id") val comicId: Long,
    @ColumnInfo(name = "chapter_id") val chapterId: Long? = null,
    @ColumnInfo(name = "current_page") val currentPage: Int = 0,
    @ColumnInfo(name = "total_pages") val totalPages: Int = 0,
    val percentage: Float = 0f,
    @ColumnInfo(name = "last_read_at") val lastReadAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "started_at") val startedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "finished_at") val finishedAt: Long? = null
)

@Entity(
    tableName = "bookmarks",
    foreignKeys = [ForeignKey(
        entity = ComicEntity::class,
        parentColumns = ["id"],
        childColumns = ["comic_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("comic_id")]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "comic_id") val comicId: Long,
    @ColumnInfo(name = "chapter_id") val chapterId: Long? = null,
    @ColumnInfo(name = "page_number") val pageNumber: Int,
    val label: String? = null,
    val note: String? = null,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "download_tasks",
    foreignKeys = [ForeignKey(
        entity = ComicEntity::class,
        parentColumns = ["id"],
        childColumns = ["comic_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("comic_id")]
)
data class DownloadTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "comic_id") val comicId: Long,
    @ColumnInfo(name = "chapter_id") val chapterId: Long,
    @ColumnInfo(name = "chapter_title") val chapterTitle: String,
    val status: String = "PENDING",
    val progress: Float = 0f,
    @ColumnInfo(name = "total_bytes") val totalBytes: Long = 0,
    @ColumnInfo(name = "downloaded_bytes") val downloadedBytes: Long = 0,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,
    val priority: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "sort_order") val order: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "comic_category",
    primaryKeys = ["comic_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = ComicEntity::class,
            parentColumns = ["id"],
            childColumns = ["comic_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("comic_id"), Index("category_id")]
)
data class ComicCategoryEntity(
    @ColumnInfo(name = "comic_id") val comicId: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long
)

@Entity(
    tableName = "tracking",
    foreignKeys = [ForeignKey(
        entity = ComicEntity::class,
        parentColumns = ["id"],
        childColumns = ["comic_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("comic_id")]
)
data class TrackingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "comic_id") val comicId: Long,
    @ColumnInfo(name = "tracker_id") val trackerId: String,
    @ColumnInfo(name = "remote_id") val remoteId: String,
    val title: String,
    @ColumnInfo(name = "last_chapter_read") val lastChapterRead: Float = 0f,
    @ColumnInfo(name = "total_chapters") val totalChapters: Int = 0,
    val score: Float = 0f,
    val status: String = "READING",
    @ColumnInfo(name = "start_date") val startDate: String? = null,
    @ColumnInfo(name = "finish_date") val finishDate: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    @ColumnInfo(name = "searched_at") val searchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "extension_repos")
data class ExtensionRepoEntity(
    @PrimaryKey
    val url: String,
    val name: String,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "manga_groups",
    indices = [Index("group_id")]
)
data class MangaGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "comic_id") val comicId: Long,
    @ColumnInfo(name = "group_id") val groupId: Long,
)
