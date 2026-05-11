package com.comi.reader.ui.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.data.repository.ComicRepository
import com.comi.reader.domain.model.Comic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortMode {
    TITLE, RECENTLY_ADDED, RECENTLY_READ, AUTHOR
}

enum class DisplayMode {
    GRID, LIST
}

data class LibraryUiState(
    val comics: List<Comic> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val sortMode: SortMode = SortMode.RECENTLY_ADDED,
    val displayMode: DisplayMode = DisplayMode.GRID,
    val showFavoritesOnly: Boolean = false,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: ComicRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortMode = MutableStateFlow(SortMode.RECENTLY_ADDED)
    private val _displayMode = MutableStateFlow(DisplayMode.GRID)
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.getAllComics(),
        _searchQuery,
        _sortMode,
        _displayMode,
        _showFavoritesOnly
    ) { comics, query, sort, display, favOnly ->
        _isLoading.value = false
        val filtered = comics
            .filter { comic ->
                if (favOnly) comic.isFavorite else true
            }
            .filter { comic ->
                if (query.isBlank()) true
                else comic.title.contains(query, ignoreCase = true) ||
                    (comic.author?.contains(query, ignoreCase = true) == true) ||
                    (comic.series?.contains(query, ignoreCase = true) == true)
            }
            .sortedWith(
                when (sort) {
                    SortMode.TITLE -> compareBy { it.title.lowercase() }
                    SortMode.RECENTLY_ADDED -> compareByDescending { it.addedAt }
                    SortMode.RECENTLY_READ -> compareByDescending { it.updatedAt }
                    SortMode.AUTHOR -> compareBy { it.author?.lowercase() ?: "" }
                }
            )

        LibraryUiState(
            comics = filtered,
            isLoading = false,
            searchQuery = query,
            sortMode = sort,
            displayMode = display,
            showFavoritesOnly = favOnly,
            error = _error.value
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LibraryUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun setDisplayMode(mode: DisplayMode) {
        _displayMode.value = mode
    }

    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun importComic(uri: Uri) {
        viewModelScope.launch {
            try {
                repository.importComic(uri)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to import: ${e.message}"
            }
        }
    }

    fun toggleFavorite(comicId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(comicId)
        }
    }

    fun deleteComic(comicId: Long) {
        viewModelScope.launch {
            repository.deleteComic(comicId, deleteFile = true)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
