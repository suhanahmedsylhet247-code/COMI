package com.comi.reader.data.repository

import android.content.Context
import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.local.entity.ChapterEntity
import com.comi.reader.extension.manager.ExtensionManager
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceDownloader @Inject constructor(
    private val context: Context,
    private val client: OkHttpClient,
    private val extensionManager: ExtensionManager,
    private val comicDao: ComicDao,
) {
    private val downloadDir: File
        get() = File(context.filesDir, "downloads").also { it.mkdirs() }

    suspend fun downloadChapter(
        comicId: Long,
        chapter: ChapterEntity,
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val comic = comicDao.getComicById(comicId) ?: return@withContext false
            val sourceId = comic.sourceId?.toLongOrNull() ?: return@withContext false
            val source = extensionManager.getCatalogueSource(sourceId) ?: return@withContext false

            val sChapter = SChapter.create().apply {
                url = chapter.url ?: return@withContext false
                name = chapter.title
            }

            val pages = source.getPageList(sChapter)
            if (pages.isEmpty()) return@withContext false

            val chapterDir = File(downloadDir, "$comicId/${chapter.id}")
            chapterDir.mkdirs()

            for (page in pages) {
                val imageUrl = page.imageUrl ?: continue
                val request = if (source is HttpSource) {
                    source.imageRequest(Page(page.index, imageUrl = imageUrl))
                } else {
                    Request.Builder().url(imageUrl).build()
                }

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) continue

                val file = File(chapterDir, "page_${page.index}.jpg")
                file.outputStream().use { output ->
                    response.body?.byteStream()?.use { it.copyTo(output) }
                }
            }

            // Mark chapter as downloaded
            comicDao.updateChapter(
                chapter.copy(
                    isDownloaded = true,
                    filePath = chapterDir.absolutePath,
                    pageCount = pages.size,
                )
            )

            true
        } catch (e: Exception) {
            android.util.Log.e("SourceDownloader", "Download failed for chapter ${chapter.id}", e)
            false
        }
    }

    fun isChapterDownloaded(comicId: Long, chapterId: Long): Boolean {
        val dir = File(downloadDir, "$comicId/$chapterId")
        return dir.exists() && (dir.listFiles()?.isNotEmpty() == true)
    }

    fun deleteChapterDownload(comicId: Long, chapterId: Long): Boolean {
        val dir = File(downloadDir, "$comicId/$chapterId")
        return dir.deleteRecursively()
    }

    fun getDownloadedPages(comicId: Long, chapterId: Long): List<File> {
        val dir = File(downloadDir, "$comicId/$chapterId")
        if (!dir.exists()) return emptyList()
        return dir.listFiles()
            ?.filter { it.isFile }
            ?.sortedBy { it.nameWithoutExtension.removePrefix("page_").toIntOrNull() ?: 0 }
            ?: emptyList()
    }
}
