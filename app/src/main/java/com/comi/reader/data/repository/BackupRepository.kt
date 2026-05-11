package com.comi.reader.data.repository

import android.content.Context
import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.preferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupJson(
    val version: Int = 2,
    val createdAt: Long = System.currentTimeMillis(),
    val comics: List<BackupComic> = emptyList(),
    val categories: List<BackupCategory> = emptyList()
)

@Serializable
data class BackupComic(
    val title: String,
    val author: String? = null,
    val artist: String? = null,
    val description: String? = null,
    val filePath: String,
    val format: String,
    val genre: String? = null,
    val tags: String? = null,
    val status: String = "UNKNOWN",
    val category: String? = null,
    val isFavorite: Boolean = false,
    val sourceId: String? = null,
    val remoteUrl: String? = null
)

@Serializable
data class BackupCategory(
    val name: String,
    val order: Int = 0
)

@Singleton
class BackupRepository @Inject constructor(
    private val dao: ComicDao,
    private val preferences: AppPreferences,
    @ApplicationContext private val context: Context
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun createBackup(): File = withContext(Dispatchers.IO) {
        val comics = dao.getAllComics().first()
        val categories = dao.getAllCategories().first()

        val backup = BackupJson(
            comics = comics.map { comic ->
                BackupComic(
                    title = comic.title,
                    author = comic.author,
                    artist = comic.artist,
                    description = comic.description,
                    filePath = comic.filePath,
                    format = comic.format,
                    genre = comic.genre,
                    tags = comic.tags,
                    status = comic.status,
                    category = comic.category,
                    isFavorite = comic.isFavorite,
                    sourceId = comic.sourceId,
                    remoteUrl = comic.remoteUrl
                )
            },
            categories = categories.map { cat ->
                BackupCategory(name = cat.name, order = cat.order)
            }
        )

        val backupDir = File(context.filesDir, "backups")
        backupDir.mkdirs()
        val timestamp = Instant.now().atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val file = File(backupDir, "comi_backup_$timestamp.json")
        file.writeText(json.encodeToString(backup))

        preferences.setLastBackupTime(System.currentTimeMillis())
        file
    }

    suspend fun restoreBackup(backupFile: File): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val content = backupFile.readText()
            val backup = json.decodeFromString<BackupJson>(content)

            var comicsRestored = 0
            var categoriesRestored = 0

            backup.categories.forEach { cat ->
                dao.insertCategory(
                    com.comi.reader.data.local.entity.CategoryEntity(
                        name = cat.name,
                        order = cat.order
                    )
                )
                categoriesRestored++
            }

            backup.comics.forEach { comic ->
                val file = File(comic.filePath)
                if (file.exists() && dao.getComicByPath(comic.filePath) == null) {
                    dao.insertComic(
                        com.comi.reader.data.local.entity.ComicEntity(
                            title = comic.title,
                            author = comic.author,
                            artist = comic.artist,
                            description = comic.description,
                            filePath = comic.filePath,
                            format = comic.format,
                            genre = comic.genre,
                            tags = comic.tags,
                            status = comic.status,
                            category = comic.category,
                            isFavorite = comic.isFavorite,
                            sourceId = comic.sourceId,
                            remoteUrl = comic.remoteUrl
                        )
                    )
                    comicsRestored++
                }
            }

            RestoreResult(true, comicsRestored, categoriesRestored)
        } catch (e: Exception) {
            RestoreResult(false, error = e.message)
        }
    }

    fun getBackupFiles(): List<File> {
        val backupDir = File(context.filesDir, "backups")
        return backupDir.listFiles()?.filter { it.extension == "json" }?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    suspend fun deleteBackup(file: File) = withContext(Dispatchers.IO) {
        file.delete()
    }
}

data class RestoreResult(
    val success: Boolean,
    val comicsRestored: Int = 0,
    val categoriesRestored: Int = 0,
    val error: String? = null
)
