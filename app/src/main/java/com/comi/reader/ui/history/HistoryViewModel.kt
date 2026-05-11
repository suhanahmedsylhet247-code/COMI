package com.comi.reader.ui.history

import androidx.lifecycle.ViewModel
import com.comi.reader.data.repository.ComicRepository
import com.comi.reader.domain.model.HistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: ComicRepository
) : ViewModel() {
    val history: Flow<List<HistoryEntry>> = repository.getReadingHistory(50)
}
