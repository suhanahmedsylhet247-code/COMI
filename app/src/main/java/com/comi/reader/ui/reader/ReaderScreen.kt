package com.comi.reader.ui.reader

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.comi.reader.domain.model.ColorFilterMode
import com.comi.reader.domain.model.ReadingMode
import com.comi.reader.domain.model.RotationMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showModeMenu by remember { mutableStateOf(false) }

    // Keep screen on
    DisposableEffect(state.keepScreenOn) {
        val activity = context as? Activity
        if (state.keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            viewModel.onLeaveReader()
        }
    }

    // Brightness override
    LaunchedEffect(state.useCustomBrightness, state.brightnessOverride) {
        val activity = context as? Activity ?: return@LaunchedEffect
        val lp = activity.window.attributes
        lp.screenBrightness = if (state.useCustomBrightness) state.brightnessOverride else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity.window.attributes = lp
    }

    val bgColor = when (state.readerBackground) {
        1 -> Color(0xFF333333)
        2 -> Color.White
        else -> Color.Black
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        // Zoomable page display
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 5f)
            if (scale > 1f) {
                offset = Offset(
                    x = offset.x + panChange.x,
                    y = offset.y + panChange.y
                )
            } else {
                offset = Offset.Zero
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .pointerInput(state.readingMode) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val width = size.width
                            val leftZone = width * 0.33f
                            val rightZone = width * 0.66f

                            when {
                                tapOffset.x < leftZone -> viewModel.previousPage()
                                tapOffset.x > rightZone -> viewModel.nextPage()
                                else -> viewModel.toggleControls()
                            }
                        },
                        onDoubleTap = { _ ->
                            if (state.doubleTapZoom) {
                                if (scale > 1f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val bitmap = state.currentBitmap
            if (bitmap != null) {
                if (state.doublePageMode && state.secondBitmap != null) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Page ${state.currentPage + 1}",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .graphicsLayer(
                                    scaleX = scale, scaleY = scale,
                                    translationX = offset.x, translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                        Image(
                            bitmap = state.secondBitmap!!.asImageBitmap(),
                            contentDescription = "Page ${state.currentPage + 2}",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .graphicsLayer(
                                    scaleX = scale, scaleY = scale,
                                    translationX = offset.x, translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Page ${state.currentPage + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale, scaleY = scale,
                                translationX = offset.x, translationY = offset.y
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // Color filter overlay
        val filterColor = when (state.colorFilterMode) {
            ColorFilterMode.SEPIA -> Color(0xFFD2A96A).copy(alpha = state.colorFilterStrength)
            ColorFilterMode.GRAYSCALE -> Color.Gray.copy(alpha = state.colorFilterStrength)
            ColorFilterMode.NIGHT -> Color.Red.copy(alpha = state.colorFilterStrength * 0.5f)
            ColorFilterMode.CUSTOM -> Color(0xFF2196F3).copy(alpha = state.colorFilterStrength * 0.3f)
            else -> Color.Transparent
        }
        if (filterColor != Color.Transparent) {
            Box(modifier = Modifier.fillMaxSize().background(filterColor))
        }

        // Tap zone overlay (flash briefly when enabled)
        if (state.showTapZones && state.isControlsVisible) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(0.33f)
                        .fillMaxHeight()
                        .background(Color.Blue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) { Text("PREV", color = Color.White.copy(alpha = 0.7f)) }
                Box(
                    modifier = Modifier
                        .weight(0.34f)
                        .fillMaxHeight()
                        .background(Color.Green.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) { Text("MENU", color = Color.White.copy(alpha = 0.7f)) }
                Box(
                    modifier = Modifier
                        .weight(0.33f)
                        .fillMaxHeight()
                        .background(Color.Blue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) { Text("NEXT", color = Color.White.copy(alpha = 0.7f)) }
            }
        }

        // Page number indicator
        if (state.showPageNumber && state.totalPages > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (state.isControlsVisible) 140.dp else 16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "${state.currentPage + 1} / ${state.totalPages}",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }

        // Top controls
        AnimatedVisibility(
            visible = state.isControlsVisible,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        state.comic?.title ?: "",
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    state.comic?.author?.let {
                        Text(it, color = Color.White.copy(alpha = 0.7f), maxLines = 1, style = MaterialTheme.typography.bodySmall)
                    }
                }

                IconButton(onClick = { viewModel.toggleBookmark() }) {
                    Icon(
                        if (state.isCurrentPageBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        "Bookmark",
                        tint = if (state.isCurrentPageBookmarked) Color(0xFFFFD700) else Color.White
                    )
                }
                IconButton(onClick = { viewModel.toggleBookmarkPanel() }) {
                    Icon(Icons.Filled.Bookmarks, "Bookmarks", tint = Color.White)
                }
                IconButton(onClick = { viewModel.toggleSettingsPanel() }) {
                    Icon(Icons.Filled.Settings, "Settings", tint = Color.White)
                }

                Box {
                    IconButton(onClick = { showModeMenu = true }) {
                        Text(
                            when (state.readingMode) {
                                ReadingMode.LEFT_TO_RIGHT -> "LTR"
                                ReadingMode.RIGHT_TO_LEFT -> "RTL"
                                ReadingMode.VERTICAL -> "V"
                                ReadingMode.WEBTOON -> "W"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DropdownMenu(expanded = showModeMenu, onDismissRequest = { showModeMenu = false }) {
                        ReadingMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name.replace('_', ' ')) },
                                onClick = { viewModel.setReadingMode(mode); showModeMenu = false }
                            )
                        }
                    }
                }
            }
        }

        // Bottom controls
        AnimatedVisibility(
            visible = state.isControlsVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                // Page slider
                if (state.totalPages > 1) {
                    Slider(
                        value = state.currentPage.toFloat(),
                        onValueChange = { viewModel.goToPage(it.toInt()) },
                        valueRange = 0f..(state.totalPages - 1).toFloat(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }

                // Nav buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.firstPage() }) {
                        Icon(Icons.Filled.FirstPage, "First", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.previousPage() }) {
                        Icon(Icons.Filled.NavigateBefore, "Previous", tint = Color.White)
                    }
                    Text(
                        "${state.currentPage + 1} / ${state.totalPages}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.nextPage() }) {
                        Icon(Icons.Filled.NavigateNext, "Next", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.lastPage() }) {
                        Icon(Icons.Filled.LastPage, "Last", tint = Color.White)
                    }
                }
            }
        }

        // Bookmark panel bottom sheet
        if (state.showBookmarkPanel) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleBookmarkPanel() },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bookmarks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (state.bookmarks.isEmpty()) {
                        Text("No bookmarks yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn {
                            items(state.bookmarks) { bookmark ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.goToBookmark(bookmark); viewModel.toggleBookmarkPanel() }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Bookmark, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(bookmark.label ?: "Page ${bookmark.pageNumber + 1}", fontWeight = FontWeight.Medium)
                                        bookmark.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    }
                                    IconButton(onClick = { viewModel.deleteBookmark(bookmark.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Delete, "Delete", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }

        // Settings panel bottom sheet
        if (state.showSettingsPanel) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleSettingsPanel() },
                sheetState = rememberModalBottomSheetState()
            ) {
                ReaderSettingsPanel(state, viewModel)
            }
        }
    }
}

@Composable
private fun ReaderSettingsPanel(state: ReaderUiState, viewModel: ReaderViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Reader Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        // Brightness
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Brightness6, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Custom Brightness", modifier = Modifier.weight(1f))
            Switch(checked = state.useCustomBrightness, onCheckedChange = { viewModel.setUseCustomBrightness(it) })
        }
        if (state.useCustomBrightness) {
            Slider(
                value = state.brightnessOverride.coerceIn(0f, 1f),
                onValueChange = { viewModel.setBrightness(it) },
                modifier = Modifier.padding(start = 28.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Color filter
        Text("Color Filter", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorFilterMode.entries.forEach { mode ->
                val isSelected = state.colorFilterMode == mode
                Text(
                    mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            MaterialTheme.shapes.small
                        )
                        .clickable { viewModel.setColorFilterMode(mode) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }
        }
        if (state.colorFilterMode != ColorFilterMode.NONE) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Strength", modifier = Modifier.width(70.dp), fontSize = 12.sp)
                Slider(
                    value = state.colorFilterStrength,
                    onValueChange = { viewModel.setColorFilterStrength(it) },
                    valueRange = 0.05f..0.5f
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Toggles
        SettingsToggle("Show Page Number", state.showPageNumber) { viewModel.setShowPageNumber(it) }
        SettingsToggle("Keep Screen On", state.keepScreenOn) { viewModel.setKeepScreenOn(it) }
        SettingsToggle("Fullscreen", state.fullscreen) { viewModel.setFullscreen(it) }
        SettingsToggle("Animate Transitions", state.animateTransitions) { viewModel.setAnimateTransitions(it) }
        SettingsToggle("Double-Tap Zoom", state.doubleTapZoom) { viewModel.setDoubleTapZoom(it) }
        SettingsToggle("Double Page Mode", state.doublePageMode) { viewModel.setDoublePageMode(it) }
        SettingsToggle("Show Tap Zones", state.showTapZones) { viewModel.setShowPageNumber(it) }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Rotation
        Text("Rotation", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(RotationMode.FREE, RotationMode.PORTRAIT, RotationMode.LANDSCAPE).forEach { mode ->
                val isSelected = state.rotationMode == mode
                Text(
                    mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            MaterialTheme.shapes.small
                        )
                        .clickable { viewModel.setRotationMode(mode) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
