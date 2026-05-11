package com.comi.reader.ui.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.comi.reader.domain.model.Comic
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onComicClick: (Long) -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var comicToDelete by remember { mutableStateOf<Comic?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importComic(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("COMI", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavoritesOnly() }) {
                        Icon(
                            if (state.showFavoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorites"
                        )
                    }
                    IconButton(onClick = {
                        viewModel.setDisplayMode(
                            if (state.displayMode == DisplayMode.GRID) DisplayMode.LIST
                            else DisplayMode.GRID
                        )
                    }) {
                        Icon(
                            if (state.displayMode == DisplayMode.GRID) Icons.Filled.ViewList
                            else Icons.Filled.GridView,
                            contentDescription = "Display mode"
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            SortMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        viewModel.setSortMode(mode)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePicker.launch(
                        arrayOf(
                            "application/zip",
                            "application/x-cbz",
                            "application/x-cbr",
                            "application/pdf",
                            "application/octet-stream"
                        )
                    )
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Import comic")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onSearch = { },
                active = false,
                onActiveChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search comics...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            ) { }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.comics.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No comics in your library",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap + to import CBZ, CBR, or PDF files",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                state.displayMode == DisplayMode.GRID -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.comics, key = { it.id }) { comic ->
                            ComicGridItem(
                                comic = comic,
                                onClick = { onComicClick(comic.id) },
                                onLongClick = { comicToDelete = comic }
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.comics, key = { it.id }) { comic ->
                            ComicListItem(
                                comic = comic,
                                onClick = { onComicClick(comic.id) },
                                onDelete = { comicToDelete = comic }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    comicToDelete?.let { comic ->
        AlertDialog(
            onDismissRequest = { comicToDelete = null },
            title = { Text("Delete Comic") },
            text = { Text("Remove \"${comic.title}\" from your library?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteComic(comic.id)
                    comicToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { comicToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComicGridItem(comic: Comic, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = comic.coverPath?.let { File(it) },
                contentDescription = comic.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    comic.title,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                comic.author?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    "${comic.pageCount} pages · ${comic.format.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ComicListItem(comic: Comic, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = comic.coverPath?.let { File(it) },
                contentDescription = comic.title,
                modifier = Modifier
                    .size(60.dp, 85.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    comic.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                comic.author?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${comic.pageCount} pages · ${comic.format.name} · ${formatFileSize(comic.fileSize)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
