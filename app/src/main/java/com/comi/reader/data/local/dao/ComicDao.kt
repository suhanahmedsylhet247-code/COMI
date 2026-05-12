package com.comi.reader.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.comi.reader.data.local.entity.BookmarkEntity
import com.comi.reader.data.local.entity.CategoryEntity
import com.comi.reader.data.local.entity.ChapterEntity
import com.comi.reader.data.local.entity.ComicCategoryEntity
import com.comi.reader.data.local.entity.ComicEntity
import com.comi.reader.data.local.entity.DownloadTaskEntity
import com.comi.reader.data.local.entity.ReadingProgressEntity
import com.comi.reader.data.local.entity.SearchHistoryEntity
import com.comi.reader.data.local.entity.TrackingEntity
import kotlinx.coroutines.flow.Flow

data class HistoryRow(
    val comicId: Long,
    val comicTitle: String,
    val coverPath: String?,
    val currentPage: Int,
    val totalPages: Int,
    val percentage: Float,
    val lastReadAt: Long
)

@Dao
interface ComicDao {

    // Comics
    @Query("SELECT * FROM comics ORDER BY added_at DESC")
    fun getAllComics(): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE is_favorite = 1 ORDER BY title ASC")
    fun getFavoriteComics(): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR series LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchComics(query: String): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE category = :category ORDER BY title ASC")
    fun getComicsByCategory(category: String): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE status = :status ORDER BY title ASC")
    fun getComicsByStatus(status: String): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE genre LIKE '%' || :genre || '%' ORDER BY title ASC")
    fun getComicsByGenre(genre: String): Flow<List<ComicEntity>>

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

    @Query("SELECT DISTINCT category FROM comics WHERE category IS NOT NULL")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT genre FROM comics WHERE genre IS NOT NULL")
    fun getGenres(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM comics")
    fun getComicCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM comics WHERE is_favorite = 1")
    fun getFavoriteCount(): Flow<Int>

    // Chapters
    @Query("SELECT * FROM chapters WHERE comic_id = :comicId ORDER BY number ASC")
    fun getChaptersForComic(comicId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE comic_id = :comicId ORDER BY number DESC")
    fun getChaptersForComicDesc(comicId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): ChapterEntity?

    @Query("SELECT COUNT(*) FROM chapters WHERE comic_id = :comicId")
    suspend fun getChapterCount(comicId: Long): Int

    @Query("SELECT COUNT(*) FROM chapters WHERE comic_id = :comicId AND is_read = 1")
    suspend fun getReadChapterCount(comicId: Long): Int

    @Query("SELECT COUNT(*) FROM chapters WHERE comic_id = :comicId AND is_downloaded = 1")
    suspend fun getDownloadedChapterCount(comicId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("UPDATE chapters SET is_read = :isRead WHERE id = :chapterId")
    suspend fun setChapterRead(chapterId: Long, isRead: Boolean)

    @Query("UPDATE chapters SET is_read = 1 WHERE comic_id = :comicId")
    suspend fun markAllChaptersRead(comicId: Long)

    @Query("UPDATE chapters SET is_read = 0 WHERE comic_id = :comicId")
    suspend fun markAllChaptersUnread(comicId: Long)

    @Query("UPDATE chapters SET is_read = 1 WHERE comic_id = :comicId AND number <= :upToNumber")
    suspend fun markChaptersReadUpTo(comicId: Long, upToNumber: Float)

    @Query("UPDATE chapters SET is_bookmarked = :bookmarked WHERE id = :chapterId")
    suspend fun setChapterBookmarked(chapterId: Long, bookmarked: Boolean)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE comic_id = :comicId AND is_downloaded = 1")
    suspend fun deleteDownloadedChapters(comicId: Long)

    // Reading progress
    @Query("SELECT * FROM reading_progress WHERE comic_id = :comicId")
    suspend fun getProgress(comicId: Long): ReadingProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ReadingProgressEntity): Long

    @Update
    suspend fun updateProgress(progress: ReadingProgressEntity)

    @Query("""
        SELECT c.id as comicId, c.title as comicTitle, c.cover_path as coverPath,
               p.current_page as currentPage, p.total_pages as totalPages,
               p.percentage, p.last_read_at as lastReadAt
        FROM reading_progress p
        JOIN comics c ON c.id = p.comic_id
        ORDER BY p.last_read_at DESC
        LIMIT :limit
    """)
    fun getReadingHistory(limit: Int): Flow<List<HistoryRow>>

    @Query("DELETE FROM reading_progress WHERE comic_id = :comicId")
    suspend fun deleteProgress(comicId: Long)

    @Query("DELETE FROM reading_progress")
    suspend fun clearAllHistory()

    @Query("SELECT COUNT(*) FROM reading_progress WHERE finished_at IS NOT NULL")
    fun getCompletedCount(): Flow<Int>

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE comic_id = :comicId ORDER BY page_number ASC")
    fun getBookmarksForComic(comicId: Long): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY created_at DESC LIMIT :limit")
    fun getRecentBookmarks(limit: Int): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM bookmarks WHERE comic_id = :comicId")
    suspend fun deleteAllBookmarksForComic(comicId: Long)

    // Downloads
    @Query("SELECT * FROM download_tasks ORDER BY priority DESC, created_at ASC")
    fun getDownloadTasks(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status IN ('PENDING', 'DOWNLOADING') ORDER BY priority DESC, created_at ASC")
    fun getActiveDownloads(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status = 'PENDING' ORDER BY priority DESC, created_at ASC LIMIT 1")
    suspend fun getNextPendingDownload(): DownloadTaskEntity?

    @Query("SELECT * FROM download_tasks WHERE comic_id = :comicId")
    fun getDownloadsForComic(comicId: Long): Flow<List<DownloadTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadTask(task: DownloadTaskEntity): Long

    @Update
    suspend fun updateDownloadTask(task: DownloadTaskEntity)

    @Delete
    suspend fun deleteDownloadTask(task: DownloadTaskEntity)

    @Query("UPDATE download_tasks SET status = 'CANCELLED' WHERE status IN ('PENDING', 'DOWNLOADING')")
    suspend fun cancelAllDownloads()

    @Query("UPDATE download_tasks SET status = 'PAUSED' WHERE status = 'DOWNLOADING'")
    suspend fun pauseAllDownloads()

    @Query("UPDATE download_tasks SET status = 'PENDING' WHERE status = 'PAUSED'")
    suspend fun resumeAllDownloads()

    @Query("UPDATE download_tasks SET status = 'PENDING', retry_count = retry_count + 1 WHERE status = 'FAILED' AND retry_count < 3")
    suspend fun retryFailedDownloads()

    @Query("DELETE FROM download_tasks WHERE status IN ('COMPLETED', 'CANCELLED')")
    suspend fun clearFinishedDownloads()

    // Categories
    @Query("SELECT * FROM categories ORDER BY sort_order ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertComicCategory(comicCategory: ComicCategoryEntity)

    @Delete
    suspend fun deleteComicCategory(comicCategory: ComicCategoryEntity)

    @Query("DELETE FROM comic_category WHERE comic_id = :comicId")
    suspend fun removeComicFromAllCategories(comicId: Long)

    @Query("""
        SELECT c.* FROM comics c
        INNER JOIN comic_category cc ON c.id = cc.comic_id
        WHERE cc.category_id = :categoryId
        ORDER BY c.title ASC
    """)
    fun getComicsInCategory(categoryId: Long): Flow<List<ComicEntity>>

    @Query("SELECT category_id FROM comic_category WHERE comic_id = :comicId")
    fun getCategoryIdsForComic(comicId: Long): Flow<List<Long>>

    // Tracking
    @Query("SELECT * FROM tracking WHERE comic_id = :comicId")
    fun getTrackingForComic(comicId: Long): Flow<List<TrackingEntity>>

    @Query("SELECT * FROM tracking WHERE comic_id = :comicId AND tracker_id = :trackerId")
    suspend fun getTracking(comicId: Long, trackerId: String): TrackingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracking(tracking: TrackingEntity): Long

    @Update
    suspend fun updateTracking(tracking: TrackingEntity)

    @Query("DELETE FROM tracking WHERE id = :id")
    suspend fun deleteTrackingById(id: Long)

    // Search history
    @Query("SELECT * FROM search_history ORDER BY searched_at DESC LIMIT :limit")
    fun getSearchHistory(limit: Int): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchHistoryEntry(id: Long)

    // Extension repos
    @Query("SELECT * FROM extension_repos ORDER BY added_at ASC")
    fun getAllExtensionRepos(): Flow<List<com.comi.reader.data.local.entity.ExtensionRepoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtensionRepo(repo: com.comi.reader.data.local.entity.ExtensionRepoEntity)

    @Query("DELETE FROM extension_repos WHERE url = :url")
    suspend fun deleteExtensionRepo(url: String)

    // Manga groups (for cross-source deduplication)
    @Query("SELECT * FROM manga_groups WHERE comic_id = :comicId")
    suspend fun getMangaGroupForComic(comicId: Long): com.comi.reader.data.local.entity.MangaGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangaGroup(group: com.comi.reader.data.local.entity.MangaGroupEntity)

    @Query("DELETE FROM manga_groups WHERE comic_id = :comicId")
    suspend fun deleteMangaGroup(comicId: Long)

    @Query("UPDATE manga_groups SET group_id = :newGroupId WHERE group_id = :oldGroupId")
    suspend fun updateMangaGroupId(oldGroupId: Long, newGroupId: Long)

    @Query("""
        SELECT c.* FROM comics c
        INNER JOIN manga_groups mg ON c.id = mg.comic_id
        WHERE mg.group_id = (SELECT group_id FROM manga_groups WHERE comic_id = :comicId LIMIT 1)
    """)
    suspend fun getComicsInGroup(comicId: Long): List<ComicEntity>

    @Query("""
        SELECT c.* FROM comics c
        INNER JOIN manga_groups mg ON c.id = mg.comic_id
        WHERE mg.group_id = (SELECT group_id FROM manga_groups WHERE comic_id = :comicId LIMIT 1)
    """)
    fun getComicsInGroupFlow(comicId: Long): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics")
    suspend fun getAllComicsSnapshot(): List<ComicEntity>

    // Source-specific queries
    @Query("SELECT * FROM comics WHERE source_id = :sourceId AND remote_url = :remoteUrl LIMIT 1")
    suspend fun getComicBySourceAndUrl(sourceId: String, remoteUrl: String): ComicEntity?

    @Query("SELECT * FROM comics WHERE source_id IS NOT NULL ORDER BY updated_at DESC")
    fun getSourceComics(): Flow<List<ComicEntity>>

    @Query("SELECT DISTINCT source_id FROM comics WHERE source_id IS NOT NULL")
    suspend fun getUsedSourceIds(): List<String>

    @Query("SELECT * FROM chapters WHERE comic_id = :comicId ORDER BY number ASC")
    suspend fun getChaptersForComicSnapshot(comicId: Long): List<ChapterEntity>

    // Chapters by source
    @Query("SELECT * FROM chapters WHERE comic_id IN (SELECT id FROM comics WHERE source_id = :sourceId) ORDER BY number ASC")
    suspend fun getChaptersBySource(sourceId: String): List<ChapterEntity>
}
