package com.comi.reader.ui.source

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.local.entity.ChapterEntity
import com.comi.reader.data.local.entity.ComicEntity
import com.comi.reader.extension.manager.ExtensionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MangaDetailUiState(
    val title: String = "",
    val author: String? = null,
    val artist: String? = null,
    val description: String? = null,
    val genre: String? = null,
    val thumbnailUrl: String? = null,
    val statusText: String = "Unknown",
    val chapters: List<SChapter> = emptyList(),
    val isLoading: Boolean = true,
    val isInLibrary: Boolean = false,
    val mangaUrl: String = "",
    val sourceId: Long = 0,
)

@HiltViewModel
class MangaDetailViewModel @Inject constructor(
    private val extensionManager: ExtensionManager,
    private val comicDao: ComicDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MangaDetailUiState())
    val uiState: StateFlow<MangaDetailUiState> = _uiState.asStateFlow()

    private var source: CatalogueSource? = null
    private var manga: SManga? = null

    fun loadManga(mangaUrl: String, sourceId: Long) {
        if (_uiState.value.mangaUrl == mangaUrl && _uiState.value.sourceId == sourceId && !_uiState.value.isLoading) return

        source = extensionManager.getCatalogueSource(sourceId)
        _uiState.update { it.copy(mangaUrl = mangaUrl, sourceId = sourceId, isLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val src = source ?: return@launch
                val sManga = SManga.create().apply { url = mangaUrl }

                // Fetch manga details
                val details = src.getMangaDetails(sManga)
                details.url = mangaUrl
                manga = details

                // Fetch chapters
                val chapters = src.getChapterList(sManga)

                // Check if in library
                val existing = comicDao.getComicBySourceAndUrl(sourceId.toString(), mangaUrl)

                _uiState.update {
                    it.copy(
                        title = details.title,
                        author = details.author,
                        artist = details.artist,
                        description = details.description,
                        genre = details.genre,
                        thumbnailUrl = details.thumbnail_url,
                        statusText = statusToText(details.status),
                        chapters = chapters,
                        isLoading = false,
                        isInLibrary = existing != null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleLibrary() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val existing = comicDao.getComicBySourceAndUrl(state.sourceId.toString(), state.mangaUrl)

            if (existing != null) {
                comicDao.deleteComic(existing)
                _uiState.update { it.copy(isInLibrary = false) }
            } else {
                val entity = ComicEntity(
                    title = state.title,
                    author = state.author,
                    artist = state.artist,
                    description = state.description,
                    genre = state.genre,
                    coverPath = state.thumbnailUrl,
                    filePath = "",
                    format = "SOURCE",
                    sourceId = state.sourceId.toString(),
                    remoteUrl = state.mangaUrl,
                    isFavorite = true,
                )
                val comicId = comicDao.insertComic(entity)

                // Insert chapters
                for (ch in state.chapters) {
                    comicDao.insertChapter(
                        ChapterEntity(
                            comicId = comicId,
                            title = ch.name,
                            number = ch.chapter_number,
                            url = ch.url,
                            scanlator = ch.scanlator,
                            dateUpload = ch.date_upload,
                            dateFetch = System.currentTimeMillis(),
                        )
                    )
                }

                _uiState.update { it.copy(isInLibrary = true) }
            }
        }
    }

    private fun statusToText(status: Int): String = when (status) {
        SManga.ONGOING -> "Ongoing"
        SManga.COMPLETED -> "Completed"
        SManga.LICENSED -> "Licensed"
        SManga.PUBLISHING_FINISHED -> "Publishing Finished"
        SManga.CANCELLED -> "Cancelled"
        SManga.ON_HIATUS -> "On Hiatus"
        else -> "Unknown"
    }
}
