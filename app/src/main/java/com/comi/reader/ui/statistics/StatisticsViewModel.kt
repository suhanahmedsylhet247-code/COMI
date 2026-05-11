package com.comi.reader.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.data.preferences.AppPreferences
import com.comi.reader.data.repository.ComicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val chaptersRead: Int = 0,
    val readingTimeMinutes: Long = 0,
    val totalComics: Int = 0,
    val favorites: Int = 0,
    val completed: Int = 0,
    val streak: Int = 0
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: ComicRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val chaptersRead = preferences.totalChaptersRead.first()
            val readingTime = preferences.totalReadingTimeMins.first()
            val streak = preferences.readingStreakDays.first()
            val totalComics = repository.getComicCount().first()
            val favorites = repository.getFavoriteCount().first()
            val completed = repository.getCompletedCount().first()

            _uiState.value = StatisticsUiState(
                chaptersRead = chaptersRead,
                readingTimeMinutes = readingTime,
                totalComics = totalComics,
                favorites = favorites,
                completed = completed,
                streak = streak
            )
        }
    }
}
