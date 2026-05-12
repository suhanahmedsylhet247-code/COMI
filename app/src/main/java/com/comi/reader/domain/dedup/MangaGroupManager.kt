package com.comi.reader.domain.dedup

import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.local.entity.ComicEntity
import com.comi.reader.data.local.entity.MangaGroupEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MangaGroupManager @Inject constructor(
    private val comicDao: ComicDao,
) {
    suspend fun linkManga(comicId1: Long, comicId2: Long) {
        val group1 = comicDao.getMangaGroupForComic(comicId1)
        val group2 = comicDao.getMangaGroupForComic(comicId2)

        when {
            group1 != null && group2 != null -> {
                // Merge groups: change all group2 members to group1's groupId
                comicDao.updateMangaGroupId(group2.groupId, group1.groupId)
            }
            group1 != null -> {
                comicDao.insertMangaGroup(MangaGroupEntity(comicId = comicId2, groupId = group1.groupId))
            }
            group2 != null -> {
                comicDao.insertMangaGroup(MangaGroupEntity(comicId = comicId1, groupId = group2.groupId))
            }
            else -> {
                // Create new group
                val groupId = System.currentTimeMillis()
                comicDao.insertMangaGroup(MangaGroupEntity(comicId = comicId1, groupId = groupId))
                comicDao.insertMangaGroup(MangaGroupEntity(comicId = comicId2, groupId = groupId))
            }
        }
    }

    suspend fun unlinkManga(comicId: Long) {
        comicDao.deleteMangaGroup(comicId)
    }

    suspend fun getGroupMembers(comicId: Long): List<ComicEntity> {
        val group = comicDao.getMangaGroupForComic(comicId) ?: return listOf(
            comicDao.getComicById(comicId) ?: return emptyList()
        )
        return comicDao.getComicsInGroup(comicId)
    }

    fun getGroupMembersFlow(comicId: Long): Flow<List<ComicEntity>> {
        return comicDao.getComicsInGroupFlow(comicId)
    }

    suspend fun autoDetectSameTitle(comic: ComicEntity): List<ComicEntity> {
        val normalizedTitle = normalizeTitle(comic.title)
        val allComics = comicDao.getAllComicsSnapshot()
        return allComics.filter { other ->
            other.id != comic.id &&
                other.sourceId != comic.sourceId &&
                normalizeTitle(other.title) == normalizedTitle
        }
    }

    private fun normalizeTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }
}
