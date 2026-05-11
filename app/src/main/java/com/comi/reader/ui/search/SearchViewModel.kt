package com.comi.reader.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.data.repository.ComicRepository
import com.comi.reader.domain.model.Comic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Comic> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val isSearchActive: Boolean = false,
    val statusFilter: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ComicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
        loadGenres()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            repository.getSearchHistory(10).collect { history ->
                _uiState.value = _uiState.value.copy(searchHistory = history)
            }
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            repository.getGenres().collect { genres ->
                _uiState.value = _uiState.value.copy(genres = genres)
            }
        }
    }

    fun setQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        if (query.length >= 2) {
            viewModelScope.launch {
                val results = repository.searchComics(query).first()
                _uiState.value = _uiState.value.copy(results = applyFilters(results))
            }
        } else if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(results = emptyList())
        }
    }

    fun search() {
        val query = _uiState.value.query
        if (query.isBlank()) return
        viewModelScope.launch {
            repository.addSearchHistory(query)
            val results = repository.searchComics(query).first()
            _uiState.value = _uiState.value.copy(
                results = applyFilters(results),
                isSearchActive = false
            )
        }
    }

    fun clearQuery() {
        _uiState.value = _uiState.value.copy(query = "", results = emptyList())
    }

    fun setSearchActive(active: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchActive = active)
    }

    fun setStatusFilter(status: String?) {
        _uiState.value = _uiState.value.copy(statusFilter = status)
        if (_uiState.value.query.isNotBlank()) search()
    }

    fun clearSearchHistory() {
        viewModelScope.launch { repository.clearSearchHistory() }
    }

    private fun applyFilters(comics: List<Comic>): List<Comic> {
        val statusFilter = _uiState.value.statusFilter
        return if (statusFilter != null) {
            comics.filter { it.status.name == statusFilter }
        } else comics
    }
}
