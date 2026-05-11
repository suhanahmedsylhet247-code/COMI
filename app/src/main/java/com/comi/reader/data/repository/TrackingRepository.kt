package com.comi.reader.data.repository

import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.local.entity.TrackingEntity
import com.comi.reader.domain.model.TrackingEntry
import com.comi.reader.domain.model.TrackingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepository @Inject constructor(
    private val dao: ComicDao
) {
    fun getTrackingForComic(comicId: Long): Flow<List<TrackingEntry>> =
        dao.getTrackingForComic(comicId).map { list -> list.map { it.toDomain() } }

    suspend fun getTracking(comicId: Long, trackerId: String): TrackingEntry? =
        dao.getTracking(comicId, trackerId)?.toDomain()

    suspend fun addTracking(entry: TrackingEntry): Long {
        return dao.insertTracking(entry.toEntity())
    }

    suspend fun updateTracking(entry: TrackingEntry) {
        dao.updateTracking(entry.toEntity())
    }

    suspend fun removeTracking(id: Long) {
        dao.deleteTrackingById(id)
    }

    suspend fun updateChapterProgress(comicId: Long, trackerId: String, chapterNumber: Float) {
        dao.getTracking(comicId, trackerId)?.let { entity ->
            dao.updateTracking(entity.copy(
                lastChapterRead = chapterNumber,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }

    suspend fun updateScore(comicId: Long, trackerId: String, score: Float) {
        dao.getTracking(comicId, trackerId)?.let { entity ->
            dao.updateTracking(entity.copy(score = score, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun updateStatus(comicId: Long, trackerId: String, status: TrackingStatus) {
        dao.getTracking(comicId, trackerId)?.let { entity ->
            dao.updateTracking(entity.copy(status = status.name, updatedAt = System.currentTimeMillis()))
        }
    }

    private fun TrackingEntity.toDomain() = TrackingEntry(
        id = id,
        comicId = comicId,
        trackerId = trackerId,
        remoteId = remoteId,
        title = title,
        lastChapterRead = lastChapterRead,
        totalChapters = totalChapters,
        score = score,
        status = try { TrackingStatus.valueOf(status) } catch (_: Exception) { TrackingStatus.READING },
        startDate = startDate,
        finishDate = finishDate
    )

    private fun TrackingEntry.toEntity() = TrackingEntity(
        id = id,
        comicId = comicId,
        trackerId = trackerId,
        remoteId = remoteId,
        title = title,
        lastChapterRead = lastChapterRead,
        totalChapters = totalChapters,
        score = score,
        status = status.name,
        startDate = startDate,
        finishDate = finishDate
    )
}
