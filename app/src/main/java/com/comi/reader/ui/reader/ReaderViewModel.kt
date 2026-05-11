package com.comi.reader.ui.reader

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.data.preferences.AppPreferences
import com.comi.reader.data.repository.ComicRepository
import com.comi.reader.domain.model.Bookmark
import com.comi.reader.domain.model.ColorFilterMode
import com.comi.reader.domain.model.Comic
import com.comi.reader.domain.model.ReadingMode
import com.comi.reader.domain.model.RotationMode
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
    val secondBitmap: Bitmap? = null,
    val readingMode: ReadingMode = ReadingMode.LEFT_TO_RIGHT,
    val isControlsVisible: Boolean = true,
    val isLoading: Boolean = true,
    val bookmarks: List<Bookmark> = emptyList(),
    val isCurrentPageBookmarked: Boolean = false,
    // Advanced settings
    val showPageNumber: Boolean = true,
    val keepScreenOn: Boolean = true,
    val fullscreen: Boolean = true,
    val animateTransitions: Boolean = true,
    val doubleTapZoom: Boolean = true,
    val rotationMode: RotationMode = RotationMode.FREE,
    val brightnessOverride: Float = -1f,
    val useCustomBrightness: Boolean = false,
    val colorFilterMode: ColorFilterMode = ColorFilterMode.NONE,
    val colorFilterStrength: Float = 0.25f,
    val readerBackground: Int = 0,
    val doublePageMode: Boolean = false,
    val showTapZones: Boolean = false,
    val showBookmarkPanel: Boolean = false,
    val showSettingsPanel: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ComicRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val comicId: Long = checkNotNull(savedStateHandle["comicId"])
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()
    private var readingStartTime: Long = System.currentTimeMillis()

    init {
        loadComic()
        observePreferences()
        observeBookmarks()
    }

    private fun loadComic() {
        viewModelScope.launch {
            val comic = repository.getComic(comicId)
            if (comic != null) {
                val progress = repository.getProgress(comicId)
                val startPage = progress?.currentPage ?: 0
                val mode = preferences.defaultReadingMode.stateIn(viewModelScope, SharingStarted.Eagerly, ReadingMode.LEFT_TO_RIGHT).value
                _uiState.value = _uiState.value.copy(
                    comic = comic,
                    totalPages = comic.pageCount,
                    readingMode = mode,
                    isLoading = false
                )
                goToPage(startPage)
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferences.showPageNumber.collect { _uiState.value = _uiState.value.copy(showPageNumber = it) }
        }
        viewModelScope.launch {
            preferences.keepScreenOn.collect { _uiState.value = _uiState.value.copy(keepScreenOn = it) }
        }
        viewModelScope.launch {
            preferences.fullscreen.collect { _uiState.value = _uiState.value.copy(fullscreen = it) }
        }
        viewModelScope.launch {
            preferences.animatePageTransitions.collect { _uiState.value = _uiState.value.copy(animateTransitions = it) }
        }
        viewModelScope.launch {
            preferences.doubleTapZoom.collect { _uiState.value = _uiState.value.copy(doubleTapZoom = it) }
        }
        viewModelScope.launch {
            preferences.rotationMode.collect { _uiState.value = _uiState.value.copy(rotationMode = it) }
        }
        viewModelScope.launch {
            preferences.useCustomBrightness.collect { _uiState.value = _uiState.value.copy(useCustomBrightness = it) }
        }
        viewModelScope.launch {
            preferences.brightnessOverride.collect { _uiState.value = _uiState.value.copy(brightnessOverride = it) }
        }
        viewModelScope.launch {
            preferences.colorFilterMode.collect { _uiState.value = _uiState.value.copy(colorFilterMode = it) }
        }
        viewModelScope.launch {
            preferences.colorFilterStrength.collect { _uiState.value = _uiState.value.copy(colorFilterStrength = it) }
        }
        viewModelScope.launch {
            preferences.readerBackground.collect { _uiState.value = _uiState.value.copy(readerBackground = it) }
        }
        viewModelScope.launch {
            preferences.doublePageMode.collect { _uiState.value = _uiState.value.copy(doublePageMode = it) }
        }
        viewModelScope.launch {
            preferences.showTapZones.collect { _uiState.value = _uiState.value.copy(showTapZones = it) }
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            repository.getBookmarks(comicId)
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                .collect { bookmarks ->
                    _uiState.value = _uiState.value.copy(
                        bookmarks = bookmarks,
                        isCurrentPageBookmarked = bookmarks.any { it.pageNumber == _uiState.value.currentPage }
                    )
                }
        }
    }

    fun goToPage(page: Int) {
        val state = _uiState.value
        val comic = state.comic ?: return
        val safePage = page.coerceIn(0, (state.totalPages - 1).coerceAtLeast(0))

        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                repository.extractPage(File(comic.filePath), safePage)
            }
            val secondBitmap = if (state.doublePageMode && safePage + 1 < state.totalPages) {
                withContext(Dispatchers.IO) {
                    repository.extractPage(File(comic.filePath), safePage + 1)
                }
            } else null

            _uiState.value = _uiState.value.copy(
                currentPage = safePage,
                currentBitmap = bitmap,
                secondBitmap = secondBitmap,
                isCurrentPageBookmarked = state.bookmarks.any { it.pageNumber == safePage }
            )
            repository.updateProgress(comicId, safePage, state.totalPages)
        }
    }

    fun nextPage() {
        val state = _uiState.value
        val step = if (state.doublePageMode) 2 else 1
        when (state.readingMode) {
            ReadingMode.RIGHT_TO_LEFT -> goToPage(state.currentPage - step)
            else -> goToPage(state.currentPage + step)
        }
    }

    fun previousPage() {
        val state = _uiState.value
        val step = if (state.doublePageMode) 2 else 1
        when (state.readingMode) {
            ReadingMode.RIGHT_TO_LEFT -> goToPage(state.currentPage + step)
            else -> goToPage(state.currentPage - step)
        }
    }

    fun firstPage() = goToPage(0)
    fun lastPage() = goToPage(_uiState.value.totalPages - 1)

    fun toggleControls() {
        _uiState.value = _uiState.value.copy(isControlsVisible = !_uiState.value.isControlsVisible)
    }

    fun setReadingMode(mode: ReadingMode) {
        _uiState.value = _uiState.value.copy(readingMode = mode)
        viewModelScope.launch { preferences.setDefaultReadingMode(mode) }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            val state = _uiState.value
            val existing = state.bookmarks.find { it.pageNumber == state.currentPage }
            if (existing != null) {
                repository.deleteBookmark(existing.id)
            } else {
                repository.addBookmark(comicId, state.currentPage, "Page ${state.currentPage + 1}", null)
            }
        }
    }

    fun addBookmark(label: String?, note: String?) {
        viewModelScope.launch {
            repository.addBookmark(comicId, _uiState.value.currentPage, label, note)
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch { repository.deleteBookmark(id) }
    }

    fun goToBookmark(bookmark: Bookmark) {
        goToPage(bookmark.pageNumber)
    }

    fun toggleBookmarkPanel() {
        _uiState.value = _uiState.value.copy(showBookmarkPanel = !_uiState.value.showBookmarkPanel)
    }

    fun toggleSettingsPanel() {
        _uiState.value = _uiState.value.copy(showSettingsPanel = !_uiState.value.showSettingsPanel)
    }

    // Reader settings
    fun setShowPageNumber(show: Boolean) {
        viewModelScope.launch { preferences.setShowPageNumber(show) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { preferences.setKeepScreenOn(enabled) }
    }

    fun setFullscreen(enabled: Boolean) {
        viewModelScope.launch { preferences.setFullscreen(enabled) }
    }

    fun setAnimateTransitions(enabled: Boolean) {
        viewModelScope.launch { preferences.setAnimatePageTransitions(enabled) }
    }

    fun setDoubleTapZoom(enabled: Boolean) {
        viewModelScope.launch { preferences.setDoubleTapZoom(enabled) }
    }

    fun setRotationMode(mode: RotationMode) {
        viewModelScope.launch { preferences.setRotationMode(mode) }
    }

    fun setBrightness(value: Float) {
        viewModelScope.launch { preferences.setBrightnessOverride(value) }
    }

    fun setUseCustomBrightness(enabled: Boolean) {
        viewModelScope.launch { preferences.setUseCustomBrightness(enabled) }
    }

    fun setColorFilterMode(mode: ColorFilterMode) {
        viewModelScope.launch { preferences.setColorFilterMode(mode) }
    }

    fun setColorFilterStrength(strength: Float) {
        viewModelScope.launch { preferences.setColorFilterStrength(strength) }
    }

    fun setDoublePageMode(enabled: Boolean) {
        viewModelScope.launch { preferences.setDoublePageMode(enabled) }
        goToPage(_uiState.value.currentPage)
    }

    fun onLeaveReader() {
        val elapsed = (System.currentTimeMillis() - readingStartTime) / 60000
        if (elapsed > 0) {
            viewModelScope.launch { preferences.addReadingTime(elapsed) }
        }
    }
}
