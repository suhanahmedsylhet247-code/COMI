package com.comi.reader.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.local.entity.DownloadTaskEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadUiItem(
    val id: Long,
    val comicId: Long,
    val chapterId: Long,
    val chapterTitle: String,
    val status: String,
    val progress: Float,
    val totalBytes: Long,
    val downloadedBytes: Long
)

data class DownloadsUiState(
    val tasks: List<DownloadUiItem> = emptyList()
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val dao: ComicDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getDownloadTasks().collect { tasks ->
                _uiState.value = DownloadsUiState(
                    tasks = tasks.map { it.toUiItem() }
                )
            }
        }
    }

    fun cancelTask(id: Long) {
        viewModelScope.launch {
            dao.getDownloadTasks().collect { tasks ->
                tasks.find { it.id == id }?.let { task ->
                    dao.updateDownloadTask(task.copy(status = "CANCELLED"))
                }
            }
        }
    }

    fun pauseAll() {
        viewModelScope.launch { dao.pauseAllDownloads() }
    }

    fun resumeAll() {
        viewModelScope.launch { dao.resumeAllDownloads() }
    }

    fun retryFailed() {
        viewModelScope.launch { dao.retryFailedDownloads() }
    }

    fun clearFinished() {
        viewModelScope.launch { dao.clearFinishedDownloads() }
    }

    private fun DownloadTaskEntity.toUiItem() = DownloadUiItem(
        id = id,
        comicId = comicId,
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        status = status,
        progress = progress,
        totalBytes = totalBytes,
        downloadedBytes = downloadedBytes
    )
}
