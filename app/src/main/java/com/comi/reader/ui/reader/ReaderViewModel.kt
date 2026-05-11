package com.comi.reader.ui.reader

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.data.parser.ComicParser
import com.comi.reader.data.repository.ComicRepository
import com.comi.reader.domain.model.Bookmark
import com.comi.reader.domain.model.Comic
import com.comi.reader.domain.model.ReadingMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class ReaderUiState(
    val comic: Comic? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val currentBitmap: Bitmap? = null,
    val readingMode: ReadingMode = ReadingMode.LEFT_TO_RIGHT,
    val isControlsVisible: Boolean = true,
    val isLoading: Boolean = true,
    val bookmarks: List<Bookmark> = emptyList(),
    val isCurrentPageBookmarked: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ComicRepository,
    private val parser: ComicParser
) : ViewModel() {

    private val comicId: Long = checkNotNull(savedStateHandle["comicId"])
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var comicFile: File? = null

    init {
        loadComic()
        observeBookmarks()
    }

    private fun loadComic() {
        viewModelScope.launch {
            try {
                val comic = repository.getComic(comicId)
                if (comic == null) {
                    _uiState.value = _uiState.value.copy(error = "Comic not found", isLoading = false)
                    return@launch
                }

                comicFile = File(comic.filePath)
                val progress = repository.getProgress(comicId)
                val startPage = progress?.currentPage ?: 0

                _uiState.value = _uiState.value.copy(
                    comic = comic,
                    totalPages = comic.pageCount,
                    currentPage = startPage,
                    isLoading = false
                )

                loadPage(startPage)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load comic: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            repository.getBookmarks(comicId)
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                .collect { bookmarks ->
                    val currentPage = _uiState.value.currentPage
                    _uiState.value = _uiState.value.copy(
                        bookmarks = bookmarks,
                        isCurrentPageBookmarked = bookmarks.any { it.pageNumber == currentPage }
                    )
                }
        }
    }

    private fun loadPage(pageIndex: Int) {
        viewModelScope.launch {
            val file = comicFile ?: return@launch
            val bitmap = withContext(Dispatchers.IO) {
                parser.extractPage(file, pageIndex)
            }
            _uiState.value = _uiState.value.copy(
                currentBitmap = bitmap,
                currentPage = pageIndex,
                isCurrentPageBookmarked = _uiState.value.bookmarks.any { it.pageNumber == pageIndex }
            )

            // Save progress
            val state = _uiState.value
            repository.updateProgress(comicId, pageIndex, state.totalPages)
        }
    }

    fun goToPage(page: Int) {
        val state = _uiState.value
        val clamped = page.coerceIn(0, state.totalPages - 1)
        if (clamped != state.currentPage) {
            loadPage(clamped)
        }
    }

    fun nextPage() {
        val state = _uiState.value
        if (state.currentPage < state.totalPages - 1) {
            goToPage(state.currentPage + 1)
        }
    }

    fun previousPage() {
        val state = _uiState.value
        if (state.currentPage > 0) {
            goToPage(state.currentPage - 1)
        }
    }

    fun firstPage() = goToPage(0)

    fun lastPage() {
        val state = _uiState.value
        goToPage(state.totalPages - 1)
    }

    fun toggleControls() {
        _uiState.value = _uiState.value.copy(
            isControlsVisible = !_uiState.value.isControlsVisible
        )
    }

    fun setReadingMode(mode: ReadingMode) {
        _uiState.value = _uiState.value.copy(readingMode = mode)
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isCurrentPageBookmarked) {
                state.bookmarks
                    .find { it.pageNumber == state.currentPage }
                    ?.let { repository.deleteBookmark(it.id) }
            } else {
                repository.addBookmark(comicId, state.currentPage, null, null)
            }
        }
    }

    fun addBookmark(label: String?, note: String?) {
        viewModelScope.launch {
            repository.addBookmark(comicId, _uiState.value.currentPage, label, note)
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmarkId)
        }
    }

    fun goToBookmark(bookmark: Bookmark) {
        goToPage(bookmark.pageNumber)
    }
}
