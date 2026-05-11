package com.comi.reader.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.data.preferences.AppPreferences
import com.comi.reader.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class BackupInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long
)

data class BackupUiState(
    val backups: List<BackupInfo> = emptyList(),
    val lastBackupTime: Long = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        loadBackups()
    }

    private fun loadBackups() {
        viewModelScope.launch {
            val files = backupRepository.getBackupFiles()
            val lastTime = preferences.lastBackupTime.first()
            _uiState.value = BackupUiState(
                backups = files.map { BackupInfo(it.name, it.absolutePath, it.length(), it.lastModified()) },
                lastBackupTime = lastTime
            )
        }
    }

    fun createBackup(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            backupRepository.createBackup()
            _uiState.value = _uiState.value.copy(isLoading = false)
            loadBackups()
            onComplete()
        }
    }

    fun restoreBackup(path: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = backupRepository.restoreBackup(File(path))
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.success) onComplete(result.comicsRestored)
        }
    }

    fun deleteBackup(path: String) {
        viewModelScope.launch {
            backupRepository.deleteBackup(File(path))
            loadBackups()
        }
    }
}
