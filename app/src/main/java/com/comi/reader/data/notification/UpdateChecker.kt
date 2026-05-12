package com.comi.reader.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.comi.reader.R
import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.local.entity.ChapterEntity
import com.comi.reader.domain.dedup.ChapterDeduplicator
import com.comi.reader.extension.manager.ExtensionManager
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class UpdateChecker @Inject constructor(
    private val context: Context,
    private val comicDao: ComicDao,
    private val extensionManager: ExtensionManager,
) {
    companion object {
        private const val CHANNEL_ID = "chapter_updates"
        private const val NOTIFICATION_GROUP = "com.comi.reader.CHAPTER_UPDATES"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chapter Updates",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Notifications for new chapter updates"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    suspend fun checkForUpdates() = withContext(Dispatchers.IO) {
        val comics = comicDao.getAllComicsSnapshot()
        val sourceComics = comics.filter { it.sourceId != null }

        // Group comics that are the same title across sources
        val groups = mutableMapOf<Long, MutableList<Long>>() // groupId -> comicIds
        val ungrouped = mutableListOf<Long>()

        for (comic in sourceComics) {
            val group = comicDao.getMangaGroupForComic(comic.id)
            if (group != null) {
                groups.getOrPut(group.groupId) { mutableListOf() }.add(comic.id)
            } else {
                ungrouped.add(comic.id)
            }
        }

        // Check grouped comics — deduplicate notifications across sources
        for ((_, comicIds) in groups) {
            checkGroupForUpdates(comicIds)
        }

        // Check ungrouped comics individually
        for (comicId in ungrouped) {
            checkSingleComicForUpdates(comicId)
        }
    }

    private suspend fun checkGroupForUpdates(comicIds: List<Long>) {
        val newChaptersBySource = mutableMapOf<String, List<ChapterEntity>>()
        var groupTitle = ""

        for (comicId in comicIds) {
            val comic = comicDao.getComicById(comicId) ?: continue
            if (groupTitle.isEmpty()) groupTitle = comic.title
            val sourceId = comic.sourceId ?: continue
            val source = extensionManager.getCatalogueSource(sourceId.toLongOrNull() ?: continue) ?: continue

            val newChapters = fetchNewChapters(comicId, source, comic.remoteUrl ?: continue)
            if (newChapters.isNotEmpty()) {
                newChaptersBySource[sourceId] = newChapters
            }
        }

        // Deduplicate across sources — one notification per unique chapter
        val deduplicated = ChapterDeduplicator.deduplicateNewChapters(newChaptersBySource)

        if (deduplicated.isNotEmpty()) {
            sendNotification(
                title = groupTitle,
                text = "${deduplicated.size} new chapter(s)",
                comicId = comicIds.first(),
            )
        }
    }

    private suspend fun checkSingleComicForUpdates(comicId: Long) {
        val comic = comicDao.getComicById(comicId) ?: return
        val sourceId = comic.sourceId ?: return
        val source = extensionManager.getCatalogueSource(sourceId.toLongOrNull() ?: return) ?: return

        val newChapters = fetchNewChapters(comicId, source, comic.remoteUrl ?: return)
        if (newChapters.isNotEmpty()) {
            sendNotification(
                title = comic.title,
                text = "${newChapters.size} new chapter(s)",
                comicId = comicId,
            )
        }
    }

    private suspend fun fetchNewChapters(
        comicId: Long,
        source: CatalogueSource,
        remoteUrl: String,
    ): List<ChapterEntity> {
        return try {
            val manga = SManga.create().apply { url = remoteUrl }
            val remoteChapters = source.getChapterList(manga)
            val existingChapters = comicDao.getChaptersForComicSnapshot(comicId)
            val existingNumbers = existingChapters.map { it.number }.toSet()

            remoteChapters
                .filter { sc -> !existingNumbers.any { abs(it - sc.chapter_number) < 0.01f } }
                .map { sc ->
                    ChapterEntity(
                        comicId = comicId,
                        title = sc.name,
                        number = sc.chapter_number,
                        url = sc.url,
                        scanlator = sc.scanlator,
                        dateUpload = sc.date_upload,
                        dateFetch = System.currentTimeMillis(),
                    )
                }
        } catch (e: Exception) {
            android.util.Log.e("UpdateChecker", "Failed to fetch chapters for comic $comicId", e)
            emptyList()
        }
    }

    private fun sendNotification(title: String, text: String, comicId: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setGroup(NOTIFICATION_GROUP)
            .setAutoCancel(true)
            .build()

        nm.notify(comicId.toInt(), notification)
    }
}
