package com.comi.reader.ui.reader

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.comi.reader.domain.model.ReadingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showReadingModeMenu by remember { mutableStateOf(false) }
    var showBookmarkSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            state.error != null -> {
                Text(
                    state.error!!,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            else -> {
                // Page display
                PageDisplay(
                    bitmap = state.currentBitmap,
                    readingMode = state.readingMode,
                    onTapLeft = {
                        when (state.readingMode) {
                            ReadingMode.RIGHT_TO_LEFT -> viewModel.nextPage()
                            else -> viewModel.previousPage()
                        }
                    },
                    onTapRight = {
                        when (state.readingMode) {
                            ReadingMode.RIGHT_TO_LEFT -> viewModel.previousPage()
                            else -> viewModel.nextPage()
                        }
                    },
                    onTapCenter = { viewModel.toggleControls() }
                )

                // Controls overlay
                AnimatedVisibility(
                    visible = state.isControlsVisible,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    state.comic?.title ?: "",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                state.comic?.author?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.toggleBookmark() }) {
                                Icon(
                                    if (state.isCurrentPageBookmarked) Icons.Filled.Bookmark
                                    else Icons.Filled.BookmarkBorder,
                                    "Bookmark",
                                    tint = if (state.isCurrentPageBookmarked) Color(0xFFFBBF24) else Color.White
                                )
                            }
                            Box {
                                IconButton(onClick = { showReadingModeMenu = true }) {
                                    Icon(Icons.Filled.MoreVert, "More", tint = Color.White)
                                }
                                DropdownMenu(
                                    expanded = showReadingModeMenu,
                                    onDismissRequest = { showReadingModeMenu = false }
                                ) {
                                    ReadingMode.entries.forEach { mode ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    when (mode) {
                                                        ReadingMode.LEFT_TO_RIGHT -> "Left to Right"
                                                        ReadingMode.RIGHT_TO_LEFT -> "Right to Left"
                                                        ReadingMode.VERTICAL -> "Vertical"
                                                        ReadingMode.WEBTOON -> "Webtoon"
                                                    }
                                                )
                                            },
                                            onClick = {
                                                viewModel.setReadingMode(mode)
                                                showReadingModeMenu = false
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Bookmarks") },
                                        onClick = {
                                            showBookmarkSheet = true
                                            showReadingModeMenu = false
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black.copy(alpha = 0.7f)
                        )
                    )
                }

                // Bottom controls
                AnimatedVisibility(
                    visible = state.isControlsVisible,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    BottomAppBar(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.firstPage() }) {
                                Icon(Icons.Filled.FirstPage, "First", tint = Color.White)
                            }
                            IconButton(onClick = { viewModel.previousPage() }) {
                                Icon(Icons.Filled.ChevronLeft, "Previous", tint = Color.White)
                            }

                            Slider(
                                value = if (state.totalPages > 1) {
                                    state.currentPage.toFloat() / (state.totalPages - 1)
                                } else 0f,
                                onValueChange = { fraction ->
                                    val page = (fraction * (state.totalPages - 1)).toInt()
                                    viewModel.goToPage(page)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(onClick = { viewModel.nextPage() }) {
                                Icon(Icons.Filled.ChevronRight, "Next", tint = Color.White)
                            }
                            IconButton(onClick = { viewModel.lastPage() }) {
                                Icon(Icons.Filled.LastPage, "Last", tint = Color.White)
                            }
                        }
                    }
                }

                // Page indicator
                AnimatedVisibility(
                    visible = state.isControlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp)
                ) {
                    Text(
                        "${state.currentPage + 1} / ${state.totalPages}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Bookmarks bottom sheet
    if (showBookmarkSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBookmarkSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Bookmarks", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                if (state.bookmarks.isEmpty()) {
                    Text(
                        "No bookmarks yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn {
                        items(state.bookmarks) { bookmark ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.goToBookmark(bookmark)
                                        showBookmarkSheet = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Bookmark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Page ${bookmark.pageNumber + 1}")
                                    bookmark.label?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteBookmark(bookmark.id) }) {
                                    Icon(
                                        Icons.Filled.Bookmark,
                                        "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PageDisplay(
    bitmap: Bitmap?,
    readingMode: ReadingMode,
    onTapLeft: () -> Unit,
    onTapRight: () -> Unit,
    onTapCenter: () -> Unit
) {
    if (bitmap == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val third = size.width / 3f
                    when {
                        offset.x < third -> onTapLeft()
                        offset.x > third * 2 -> onTapRight()
                        else -> onTapCenter()
                    }
                }
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Comic page",
            modifier = Modifier.fillMaxSize(),
            contentScale = when (readingMode) {
                ReadingMode.WEBTOON -> ContentScale.FillWidth
                else -> ContentScale.Fit
            }
        )
    }
}
