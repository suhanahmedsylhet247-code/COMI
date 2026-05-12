package com.comi.reader.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comi.reader.extension.manager.ExtensionManager
import com.comi.reader.extension.model.Extension
import com.comi.reader.extension.model.InstallStep
import com.comi.reader.extension.repo.ExtensionRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.kanade.tachiyomi.source.CatalogueSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrowseUiState(
    val selectedTab: BrowseTab = BrowseTab.SOURCES,
    val sources: List<CatalogueSource> = emptyList(),
    val installedExtensions: List<Extension.Installed> = emptyList(),
    val availableExtensions: List<Extension.Available> = emptyList(),
    val repos: List<ExtensionRepo> = emptyList(),
    val installSteps: Map<String, InstallStep> = emptyMap(),
    val isLoadingRepos: Boolean = false,
    val showAddRepoDialog: Boolean = false,
    val searchQuery: String = "",
)

enum class BrowseTab { SOURCES, EXTENSIONS, REPOS }

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val extensionManager: ExtensionManager,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(BrowseTab.SOURCES)
    private val _showAddRepoDialog = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<BrowseUiState> = combine(
        _selectedTab,
        extensionManager.sources,
        extensionManager.installedExtensions,
        extensionManager.repoManager.availableExtensions,
        extensionManager.repoManager.repos,
        extensionManager.installer.installSteps,
        extensionManager.repoManager.isLoading,
        _showAddRepoDialog,
        _searchQuery,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        BrowseUiState(
            selectedTab = values[0] as BrowseTab,
            sources = values[1] as List<CatalogueSource>,
            installedExtensions = values[2] as List<Extension.Installed>,
            availableExtensions = values[3] as List<Extension.Available>,
            repos = values[4] as List<ExtensionRepo>,
            installSteps = values[5] as Map<String, InstallStep>,
            isLoadingRepos = values[6] as Boolean,
            showAddRepoDialog = values[7] as Boolean,
            searchQuery = values[8] as String,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BrowseUiState())

    fun selectTab(tab: BrowseTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addRepo(url: String) {
        extensionManager.repoManager.addRepo(url)
        _showAddRepoDialog.value = false
        viewModelScope.launch {
            extensionManager.repoManager.fetchAllRepos()
        }
    }

    fun removeRepo(url: String) {
        extensionManager.repoManager.removeRepo(url)
    }

    fun showAddRepoDialog() {
        _showAddRepoDialog.value = true
    }

    fun hideAddRepoDialog() {
        _showAddRepoDialog.value = false
    }

    fun refreshRepos() {
        viewModelScope.launch {
            extensionManager.repoManager.fetchAllRepos()
        }
    }

    fun installExtension(extension: Extension.Available) {
        viewModelScope.launch {
            extensionManager.installExtension(extension)
        }
    }

    fun uninstallExtension(extension: Extension.Installed) {
        viewModelScope.launch {
            extensionManager.uninstallExtension(extension)
        }
    }

    fun updateExtension(extension: Extension.Available) {
        viewModelScope.launch {
            extensionManager.updateExtension(extension)
        }
    }
}
