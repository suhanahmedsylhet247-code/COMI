package com.comi.reader.ui.source

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.extension.manager.ExtensionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SourceTab { POPULAR, LATEST, SEARCH }

data class SourceBrowseUiState(
    val sourceId: Long = 0,
    val sourceName: String = "",
    val selectedTab: SourceTab = SourceTab.POPULAR,
    val searchQuery: String = "",
    val mangas: List<SManga> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
)

@HiltViewModel
class SourceBrowseViewModel @Inject constructor(
    private val extensionManager: ExtensionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SourceBrowseUiState())
    val uiState: StateFlow<SourceBrowseUiState> = _uiState.asStateFlow()

    private var source: CatalogueSource? = null
    private var loadJob: Job? = null

    fun setSource(sourceId: Long) {
        if (_uiState.value.sourceId == sourceId) return
        source = extensionManager.getCatalogueSource(sourceId)
        _uiState.update {
            it.copy(
                sourceId = sourceId,
                sourceName = source?.name ?: "Unknown",
                mangas = emptyList(),
            )
        }
        loadCurrentTab()
    }

    fun selectTab(tab: SourceTab) {
        _uiState.update { it.copy(selectedTab = tab, mangas = emptyList(), currentPage = 1) }
        loadCurrentTab()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isNotBlank() && _uiState.value.selectedTab == SourceTab.SEARCH) {
            loadCurrentTab()
        }
    }

    private fun loadCurrentTab() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            val src = source ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = when (_uiState.value.selectedTab) {
                    SourceTab.POPULAR -> src.getPopularManga(_uiState.value.currentPage)
                    SourceTab.LATEST -> {
                        if (src.supportsLatest) {
                            src.getLatestUpdates(_uiState.value.currentPage)
                        } else {
                            _uiState.update { it.copy(error = "Source doesn't support latest", isLoading = false) }
                            return@launch
                        }
                    }
                    SourceTab.SEARCH -> {
                        val query = _uiState.value.searchQuery
                        if (query.isBlank()) {
                            _uiState.update { it.copy(isLoading = false) }
                            return@launch
                        }
                        src.getSearchManga(_uiState.value.currentPage, query, FilterList())
                    }
                }
                _uiState.update {
                    it.copy(
                        mangas = result.mangas,
                        hasNextPage = result.hasNextPage,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        _uiState.update { it.copy(currentPage = it.currentPage + 1) }
        loadCurrentTab()
    }
}
