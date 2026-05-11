package com.comi.reader.data.repository

import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.local.entity.CategoryEntity
import com.comi.reader.data.local.entity.ComicCategoryEntity
import com.comi.reader.domain.model.Category
import com.comi.reader.domain.model.Comic
import com.comi.reader.domain.model.ComicFormat
import com.comi.reader.domain.model.MangaStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val dao: ComicDao
) {
    fun getAllCategories(): Flow<List<Category>> = dao.getAllCategories().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun createCategory(name: String): Long {
        val existing = dao.getAllCategories()
        return dao.insertCategory(CategoryEntity(name = name, order = 0))
    }

    suspend fun updateCategory(category: Category) {
        dao.updateCategory(CategoryEntity(id = category.id, name = category.name, order = category.order))
    }

    suspend fun deleteCategory(id: Long) {
        dao.deleteCategoryById(id)
    }

    suspend fun addComicToCategory(comicId: Long, categoryId: Long) {
        dao.insertComicCategory(ComicCategoryEntity(comicId, categoryId))
    }

    suspend fun removeComicFromCategory(comicId: Long, categoryId: Long) {
        dao.deleteComicCategory(ComicCategoryEntity(comicId, categoryId))
    }

    suspend fun removeComicFromAllCategories(comicId: Long) {
        dao.removeComicFromAllCategories(comicId)
    }

    fun getComicsInCategory(categoryId: Long): Flow<List<Comic>> =
        dao.getComicsInCategory(categoryId).map { list -> list.map { it.toDomain() } }

    fun getCategoryIdsForComic(comicId: Long): Flow<List<Long>> =
        dao.getCategoryIdsForComic(comicId)

    private fun CategoryEntity.toDomain() = Category(
        id = id,
        name = name,
        order = order,
        createdAt = Instant.ofEpochMilli(createdAt)
    )

    private fun com.comi.reader.data.local.entity.ComicEntity.toDomain() = Comic(
        id = id,
        title = title,
        author = author,
        artist = artist,
        description = description,
        coverPath = coverPath,
        filePath = filePath,
        format = try { ComicFormat.valueOf(format) } catch (_: Exception) { ComicFormat.UNKNOWN },
        fileSize = fileSize,
        pageCount = pageCount,
        series = series,
        volume = volume,
        publisher = publisher,
        year = year,
        genre = genre,
        tags = tags,
        status = try { MangaStatus.valueOf(status) } catch (_: Exception) { MangaStatus.UNKNOWN },
        category = category,
        isFavorite = isFavorite,
        sourceId = sourceId,
        remoteUrl = remoteUrl,
        addedAt = Instant.ofEpochMilli(addedAt),
        updatedAt = Instant.ofEpochMilli(updatedAt)
    )
}
