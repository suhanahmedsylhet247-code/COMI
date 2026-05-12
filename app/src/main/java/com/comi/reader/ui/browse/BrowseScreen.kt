package com.comi.reader.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.comi.reader.extension.model.Extension
import com.comi.reader.extension.model.InstallStep
import eu.kanade.tachiyomi.source.CatalogueSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    onSourceClick: (Long) -> Unit = {},
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse", fontWeight = FontWeight.Bold) },
                actions = {
                    if (state.selectedTab == BrowseTab.REPOS) {
                        IconButton(onClick = { viewModel.showAddRepoDialog() }) {
                            Icon(Icons.Filled.Add, "Add repo")
                        }
                    }
                    if (state.selectedTab == BrowseTab.EXTENSIONS) {
                        IconButton(onClick = { viewModel.refreshRepos() }) {
                            Icon(Icons.Filled.Refresh, "Refresh")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                BrowseTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            when (state.selectedTab) {
                BrowseTab.SOURCES -> SourcesTab(
                    sources = state.sources,
                    onSourceClick = onSourceClick,
                )
                BrowseTab.EXTENSIONS -> ExtensionsTab(
                    installed = state.installedExtensions,
                    available = state.availableExtensions,
                    installSteps = state.installSteps,
                    isLoading = state.isLoadingRepos,
                    onInstall = { viewModel.installExtension(it) },
                    onUninstall = { viewModel.uninstallExtension(it) },
                    onUpdate = { viewModel.updateExtension(it) },
                )
                BrowseTab.REPOS -> ReposTab(
                    repos = state.repos,
                    onRemoveRepo = { viewModel.removeRepo(it) },
                )
            }
        }
    }

    if (state.showAddRepoDialog) {
        AddRepoDialog(
            onAdd = { viewModel.addRepo(it) },
            onDismiss = { viewModel.hideAddRepoDialog() },
        )
    }
}

@Composable
private fun SourcesTab(
    sources: List<CatalogueSource>,
    onSourceClick: (Long) -> Unit,
) {
    if (sources.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Extension,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No sources installed",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Add a repo and install extensions from the Extensions tab",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(sources, key = { it.id }) { source ->
                SourceItem(source = source, onClick = { onSourceClick(source.id) })
            }
        }
    }
}

@Composable
private fun SourceItem(source: CatalogueSource, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Source,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                source.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                source.lang.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExtensionsTab(
    installed: List<Extension.Installed>,
    available: List<Extension.Available>,
    installSteps: Map<String, InstallStep>,
    isLoading: Boolean,
    onInstall: (Extension.Available) -> Unit,
    onUninstall: (Extension.Installed) -> Unit,
    onUpdate: (Extension.Available) -> Unit,
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        if (installed.isNotEmpty()) {
            item {
                Text(
                    "Installed",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            items(installed, key = { "installed-${it.pkgName}" }) { ext ->
                InstalledExtensionItem(ext, onUninstall)
            }
        }

        if (available.isNotEmpty()) {
            val installedPkgs = installed.map { it.pkgName }.toSet()
            val notInstalled = available.filter { it.pkgName !in installedPkgs }
            if (notInstalled.isNotEmpty()) {
                item {
                    Text(
                        "Available",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                items(notInstalled, key = { "available-${it.pkgName}" }) { ext ->
                    AvailableExtensionItem(ext, installSteps[ext.pkgName], onInstall)
                }
            }
        }

        if (installed.isEmpty() && available.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Add a repo from the Repos tab first, then refresh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun InstalledExtensionItem(
    extension: Extension.Installed,
    onUninstall: (Extension.Installed) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Extension,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                extension.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${extension.lang.uppercase()} · v${extension.versionName} · ${extension.sources.size} source(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = { onUninstall(extension) }) {
            Icon(Icons.Filled.Delete, "Uninstall", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AvailableExtensionItem(
    extension: Extension.Available,
    installStep: InstallStep?,
    onInstall: (Extension.Available) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Extension,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                extension.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${extension.lang.uppercase()} · v${extension.versionName}${if (extension.isNsfw) " · 18+" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when (installStep) {
            InstallStep.Downloading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
            InstallStep.Installing -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
            InstallStep.Installed -> Text("Installed", color = MaterialTheme.colorScheme.primary)
            InstallStep.Error -> OutlinedButton(onClick = { onInstall(extension) }) {
                Text("Retry")
            }
            else -> IconButton(onClick = { onInstall(extension) }) {
                Icon(Icons.Filled.Download, "Install")
            }
        }
    }
}

@Composable
private fun ReposTab(
    repos: List<com.comi.reader.extension.repo.ExtensionRepo>,
    onRemoveRepo: (String) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        if (repos.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Filled.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No repos added",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add a repo URL to browse and install extensions.\n\nExample repos:\n• Keiyoushi: https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json\n• Yuzino: https://raw.githubusercontent.com/yuzono/manga-repo/repo/index.min.json",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(repos, key = { it.url }) { repo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(repo.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        repo.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = { onRemoveRepo(repo.url) }) {
                    Icon(Icons.Filled.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AddRepoDialog(
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Extension Repository") },
        text = {
            Column {
                Text(
                    "Paste the URL to an extension repo index.min.json",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Repository URL") },
                    placeholder = { Text("https://raw.githubusercontent.com/.../index.min.json") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(url) },
                enabled = url.isNotBlank(),
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
