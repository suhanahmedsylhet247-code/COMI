package com.comi.reader.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.comi.reader.data.local.entity.BookmarkEntity
import com.comi.reader.data.local.entity.ChapterEntity
import com.comi.reader.data.local.entity.ComicEntity
import com.comi.reader.data.local.entity.DownloadTaskEntity
import com.comi.reader.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicDao {
    // Comics
    @Query("SELECT * FROM comics ORDER BY added_at DESC")
    fun getAllComics(): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE is_favorite = 1 ORDER BY title ASC")
    fun getFavoriteComics(): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR series LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchComics(query: String): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE category = :category ORDER BY title ASC")
    fun getComicsByCategory(category: String): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE id = :id")
    suspend fun getComicById(id: Long): ComicEntity?

    @Query("SELECT * FROM comics WHERE file_path = :path")
    suspend fun getComicByPath(path: String): ComicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComic(comic: ComicEntity): Long

    @Update
    suspend fun updateComic(comic: ComicEntity)

    @Delete
    suspend fun deleteComic(comic: ComicEntity)

    @Query("DELETE FROM comics WHERE id = :id")
    suspend fun deleteComicById(id: Long)

    @Query("SELECT DISTINCT category FROM comics WHERE category IS NOT NULL ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    // Chapters
    @Query("SELECT * FROM chapters WHERE comic_id = :comicId ORDER BY number ASC")
    fun getChaptersForComic(comicId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    // Reading Progress
    @Query("SELECT * FROM reading_progress WHERE comic_id = :comicId")
    suspend fun getProgress(comicId: Long): ReadingProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ReadingProgressEntity): Long

    @Update
    suspend fun updateProgress(progress: ReadingProgressEntity)

    @Query("""
        SELECT c.id AS comicId, c.title AS comicTitle, c.cover_path AS coverPath,
               p.current_page AS currentPage, p.total_pages AS totalPages,
               p.percentage, p.last_read_at AS lastReadAt
        FROM reading_progress p
        JOIN comics c ON c.id = p.comic_id
        WHERE p.finished_at IS NULL
        ORDER BY p.last_read_at DESC
        LIMIT :limit
    """)
    fun getReadingHistory(limit: Int = 20): Flow<List<HistoryRow>>

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE comic_id = :comicId ORDER BY page_number ASC")
    fun getBookmarksForComic(comicId: Long): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    // Downloads
    @Query("SELECT * FROM download_tasks ORDER BY id ASC")
    fun getDownloadTasks(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status = 'PENDING' OR status = 'DOWNLOADING' ORDER BY id ASC")
    fun getActiveDownloads(): Flow<List<DownloadTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadTask(task: DownloadTaskEntity): Long

    @Update
    suspend fun updateDownloadTask(task: DownloadTaskEntity)

    @Query("DELETE FROM download_tasks WHERE id = :id")
    suspend fun deleteDownloadTask(id: Long)
}

data class HistoryRow(
    val comicId: Long,
    val comicTitle: String,
    val coverPath: String?,
    val currentPage: Int,
    val totalPages: Int,
    val percentage: Float,
    val lastReadAt: Long
)
