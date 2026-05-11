package com.comi.reader.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.comi.reader.domain.model.MangaStatus
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onComicClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = state.query,
                    onQueryChange = { viewModel.setQuery(it) },
                    onSearch = { viewModel.search() },
                    expanded = state.isSearchActive,
                    onExpandedChange = { viewModel.setSearchActive(it) },
                    placeholder = { Text("Search comics...") },
                    leadingIcon = {
                        if (state.isSearchActive) {
                            IconButton(onClick = { viewModel.setSearchActive(false); onBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        } else {
                            Icon(Icons.Filled.Search, "Search")
                        }
                    },
                    trailingIcon = {
                        Row {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearQuery() }) {
                                    Icon(Icons.Filled.Clear, "Clear")
                                }
                            }
                            IconButton(onClick = { showFilters = !showFilters }) {
                                Icon(Icons.Filled.FilterList, "Filters")
                            }
                        }
                    }
                )
            },
            expanded = state.isSearchActive,
            onExpandedChange = { viewModel.setSearchActive(it) },
            modifier = Modifier.fillMaxWidth()
        ) {
            // Search suggestions / history
            if (state.query.isEmpty()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (state.searchHistory.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Searches", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.clearSearchHistory() }) { Text("Clear") }
                        }
                        state.searchHistory.forEach { query ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setQuery(query); viewModel.search() }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.History, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(12.dp))
                                Text(query)
                            }
                        }
                    }

                    if (state.genres.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Genres", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            state.genres.forEach { genre ->
                                AssistChip(onClick = { viewModel.setQuery(genre); viewModel.search() }, label = { Text(genre) })
                            }
                        }
                    }
                }
            }

            // Search results in expanded mode
            LazyColumn {
                items(state.results) { comic ->
                    SearchResultItem(comic = comic, onClick = { onComicClick(comic.id); viewModel.setSearchActive(false) })
                }
            }
        }

        // Filters
        if (showFilters) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Status Filter", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val statuses = listOf("All") + MangaStatus.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                    statuses.forEach { status ->
                        val isSelected = state.statusFilter == status || (status == "All" && state.statusFilter == null)
                        AssistChip(
                            onClick = { viewModel.setStatusFilter(if (status == "All") null else status.uppercase()) },
                            label = { Text(status) }
                        )
                    }
                }
            }
        }

        // Non-expanded results
        if (!state.isSearchActive && state.results.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.results) { comic ->
                    SearchResultItem(comic = comic, onClick = { onComicClick(comic.id) })
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(comic: Comic, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = comic.coverPath?.let { File(it) },
                contentDescription = null,
                modifier = Modifier
                    .width(60.dp)
                    .aspectRatio(0.7f)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(comic.title, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                comic.author?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                if (comic.genre != null) {
                    Text(comic.genre, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row {
                    Text(comic.format.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text("${comic.pageCount}p", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
