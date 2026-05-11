package com.comi.reader.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.data.repository.CategoryRepository
import com.comi.reader.data.repository.ComicRepository
import com.comi.reader.data.repository.TrackingRepository
import com.comi.reader.domain.model.Category
import com.comi.reader.domain.model.Chapter
import com.comi.reader.domain.model.ChapterSortMode
import com.comi.reader.domain.model.Comic
import com.comi.reader.domain.model.ReadingProgress
import com.comi.reader.domain.model.TrackingEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComicDetailUiState(
    val comic: Comic? = null,
    val chapters: List<Chapter> = emptyList(),
    val progress: ReadingProgress? = null,
    val trackingEntries: List<TrackingEntry> = emptyList(),
    val categoryIds: List<Long> = emptyList(),
    val allCategories: List<Category> = emptyList(),
    val chapterSortMode: ChapterSortMode = ChapterSortMode.BY_NUMBER_ASC,
    val selectedChapters: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val totalChapters: Int = 0,
    val readChapters: Int = 0,
    val downloadedChapters: Int = 0,
    val isLoading: Boolean = true,
    val isDescriptionExpanded: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ComicDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val comicRepository: ComicRepository,
    private val categoryRepository: CategoryRepository,
    private val trackingRepository: TrackingRepository
) : ViewModel() {

    private val comicId: Long = checkNotNull(savedStateHandle["comicId"])
    private val _uiState = MutableStateFlow(ComicDetailUiState())
    val uiState: StateFlow<ComicDetailUiState> = _uiState.asStateFlow()

    init {
        loadComicDetail()
        observeChapters()
        observeTracking()
        observeCategories()
    }

    private fun loadComicDetail() {
        viewModelScope.launch {
            val comic = comicRepository.getComic(comicId)
            val progress = comicRepository.getProgress(comicId)
            val totalChapters = comicRepository.getChapterCount(comicId)
            val readChapters = comicRepository.getReadChapterCount(comicId)
            val downloadedChapters = comicRepository.getDownloadedChapterCount(comicId)

            _uiState.value = _uiState.value.copy(
                comic = comic,
                progress = progress,
                totalChapters = totalChapters,
                readChapters = readChapters,
                downloadedChapters = downloadedChapters,
                isLoading = false
            )
        }
    }

    private fun observeChapters() {
        viewModelScope.launch {
            comicRepository.getChapters(comicId)
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                .collect { chapters ->
                    _uiState.value = _uiState.value.copy(
                        chapters = sortChapters(chapters, _uiState.value.chapterSortMode)
                    )
                }
        }
    }

    private fun observeTracking() {
        viewModelScope.launch {
            trackingRepository.getTrackingForComic(comicId)
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                .collect { entries ->
                    _uiState.value = _uiState.value.copy(trackingEntries = entries)
                }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(allCategories = categories)
                }
        }
        viewModelScope.launch {
            categoryRepository.getCategoryIdsForComic(comicId)
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                .collect { ids ->
                    _uiState.value = _uiState.value.copy(categoryIds = ids)
                }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            comicRepository.toggleFavorite(comicId)
            loadComicDetail()
        }
    }

    fun toggleDescriptionExpanded() {
        _uiState.value = _uiState.value.copy(
            isDescriptionExpanded = !_uiState.value.isDescriptionExpanded
        )
    }

    fun setChapterSortMode(mode: ChapterSortMode) {
        _uiState.value = _uiState.value.copy(
            chapterSortMode = mode,
            chapters = sortChapters(_uiState.value.chapters, mode)
        )
    }

    fun toggleChapterRead(chapterId: Long) {
        viewModelScope.launch {
            comicRepository.toggleChapterRead(chapterId)
            loadComicDetail()
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            comicRepository.markAllChaptersRead(comicId)
            loadComicDetail()
        }
    }

    fun markAllUnread() {
        viewModelScope.launch {
            comicRepository.markAllChaptersUnread(comicId)
            loadComicDetail()
        }
    }

    fun toggleSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = !_uiState.value.isSelectionMode,
            selectedChapters = emptySet()
        )
    }

    fun toggleChapterSelection(chapterId: Long) {
        val current = _uiState.value.selectedChapters.toMutableSet()
        if (chapterId in current) current.remove(chapterId) else current.add(chapterId)
        _uiState.value = _uiState.value.copy(selectedChapters = current)
    }

    fun markSelectedRead() {
        viewModelScope.launch {
            _uiState.value.selectedChapters.forEach { id ->
                comicRepository.setChapterRead(id, true)
            }
            _uiState.value = _uiState.value.copy(isSelectionMode = false, selectedChapters = emptySet())
            loadComicDetail()
        }
    }

    fun markSelectedUnread() {
        viewModelScope.launch {
            _uiState.value.selectedChapters.forEach { id ->
                comicRepository.setChapterRead(id, false)
            }
            _uiState.value = _uiState.value.copy(isSelectionMode = false, selectedChapters = emptySet())
            loadComicDetail()
        }
    }

    fun addToCategory(categoryId: Long) {
        viewModelScope.launch {
            categoryRepository.addComicToCategory(comicId, categoryId)
        }
    }

    fun removeFromCategory(categoryId: Long) {
        viewModelScope.launch {
            categoryRepository.removeComicFromCategory(comicId, categoryId)
        }
    }

    private fun sortChapters(chapters: List<Chapter>, mode: ChapterSortMode): List<Chapter> {
        return when (mode) {
            ChapterSortMode.BY_NUMBER_ASC -> chapters.sortedBy { it.number }
            ChapterSortMode.BY_NUMBER_DESC -> chapters.sortedByDescending { it.number }
            ChapterSortMode.BY_DATE_ASC -> chapters.sortedBy { it.addedAt }
            ChapterSortMode.BY_DATE_DESC -> chapters.sortedByDescending { it.addedAt }
        }
    }
}
