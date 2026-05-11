package com.comi.reader.ui.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.comi.reader.domain.model.Chapter
import com.comi.reader.domain.model.ChapterSortMode
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ComicDetailScreen(
    onBack: () -> Unit,
    onReadChapter: (Long, Long) -> Unit,
    viewModel: ComicDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    state.comic?.let { comic ->
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (comic.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                "Favorite",
                                tint = if (comic.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (state.isSelectionMode) {
                        IconButton(onClick = { viewModel.markSelectedRead() }) {
                            Icon(Icons.Filled.CheckCircle, "Mark read")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val comic = state.comic ?: return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Cover and info header
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = comic.coverPath?.let { File(it) },
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface),
                                    startY = 200f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            comic.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        comic.author?.let {
                            Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        comic.artist?.let { if (it != comic.author) Text("Art: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }

            // Status bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusItem("Status", comic.status.name.lowercase().replaceFirstChar { it.uppercase() })
                    StatusItem("Chapters", "${state.readChapters}/${state.totalChapters}")
                    StatusItem("Format", comic.format.name)
                    StatusItem("Pages", "${comic.pageCount}")
                }
            }

            // Continue reading button
            item {
                val progress = state.progress
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (progress != null && progress.percentage > 0f && progress.percentage < 1f) {
                        LinearProgressIndicator(
                            progress = { progress.percentage },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        )
                    }
                    Button(
                        onClick = {
                            val chapters = state.chapters
                            if (chapters.isNotEmpty()) {
                                onReadChapter(comic.id, chapters.first().id)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (progress != null && progress.percentage > 0f) "Continue Reading" else "Start Reading")
                    }
                }
            }

            // Description
            if (comic.description != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            comic.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (state.isDescriptionExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .animateContentSize()
                                .clickable { viewModel.toggleDescriptionExpanded() }
                        )
                    }
                }
            }

            // Genre tags
            if (comic.genre != null) {
                item {
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        comic.genre.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { genre ->
                            AssistChip(onClick = { }, label = { Text(genre, style = MaterialTheme.typography.labelSmall) })
                        }
                    }
                }
            }

            // Info items
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    comic.series?.let { InfoRow("Series", it) }
                    comic.volume?.let { InfoRow("Volume", it.toString()) }
                    comic.publisher?.let { InfoRow("Publisher", it) }
                    comic.year?.let { InfoRow("Year", it.toString()) }
                    InfoRow("File Size", formatFileSize(comic.fileSize))
                }
            }

            // Tracking section
            if (state.trackingEntries.isNotEmpty()) {
                item {
                    Text(
                        "Tracking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(state.trackingEntries) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.trackerId.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("Ch. ${entry.lastChapterRead} · ${entry.status.name}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (entry.score > 0) {
                                Text("${entry.score}/10", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Chapter header
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${state.totalChapters} Chapters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                        Icon(Icons.Filled.SelectAll, "Select", modifier = Modifier.size(20.dp))
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Filled.Sort, "Sort", modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            ChapterSortMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = { viewModel.setChapterSortMode(mode); showSortMenu = false }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("Mark all as read") }, onClick = { viewModel.markAllRead(); showSortMenu = false })
                            DropdownMenuItem(text = { Text("Mark all as unread") }, onClick = { viewModel.markAllUnread(); showSortMenu = false })
                        }
                    }
                }
            }

            // Chapter list
            items(state.chapters, key = { it.id }) { chapter ->
                ChapterItem(
                    chapter = chapter,
                    isSelected = chapter.id in state.selectedChapters,
                    isSelectionMode = state.isSelectionMode,
                    onClick = {
                        if (state.isSelectionMode) viewModel.toggleChapterSelection(chapter.id)
                        else onReadChapter(comic.id, chapter.id)
                    },
                    onLongClick = {
                        if (!state.isSelectionMode) viewModel.toggleSelectionMode()
                        viewModel.toggleChapterSelection(chapter.id)
                    },
                    onToggleRead = { viewModel.toggleChapterRead(chapter.id) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StatusItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ChapterItem(
    chapter: Chapter,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleRead: () -> Unit
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        chapter.isRead -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                chapter.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (chapter.isRead) FontWeight.Normal else FontWeight.Medium,
                color = if (chapter.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Row {
                if (chapter.scanlator != null) {
                    Text(chapter.scanlator, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                }
                if (chapter.pageCount > 0) {
                    Text("${chapter.pageCount}p", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (chapter.lastPageRead > 0 && !chapter.isRead) {
                    Spacer(Modifier.width(8.dp))
                    Text("Page ${chapter.lastPageRead}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (chapter.isDownloaded) {
            Icon(Icons.Filled.Download, "Downloaded", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
        }

        IconButton(onClick = onToggleRead, modifier = Modifier.size(32.dp)) {
            Icon(
                if (chapter.isRead) Icons.Filled.CheckCircle else Icons.Filled.CheckCircleOutline,
                "Toggle read",
                tint = if (chapter.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
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
