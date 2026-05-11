package com.comi.reader.ui.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                actions = {
                    if (state.tasks.any { it.status == "DOWNLOADING" }) {
                        IconButton(onClick = { viewModel.pauseAll() }) {
                            Icon(Icons.Filled.Pause, "Pause all")
                        }
                    }
                    if (state.tasks.any { it.status == "PAUSED" }) {
                        IconButton(onClick = { viewModel.resumeAll() }) {
                            Icon(Icons.Filled.PlayArrow, "Resume all")
                        }
                    }
                    if (state.tasks.any { it.status == "FAILED" }) {
                        IconButton(onClick = { viewModel.retryFailed() }) {
                            Icon(Icons.Filled.Refresh, "Retry failed")
                        }
                    }
                    if (state.tasks.any { it.status in listOf("COMPLETED", "CANCELLED") }) {
                        IconButton(onClick = { viewModel.clearFinished() }) {
                            Icon(Icons.Filled.Delete, "Clear finished")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.tasks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Download, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))
                Text("No downloads", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.tasks) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val statusIcon = when (task.status) {
                                    "COMPLETED" -> Icons.Filled.CheckCircle
                                    "FAILED" -> Icons.Filled.Error
                                    "CANCELLED" -> Icons.Filled.Cancel
                                    "PAUSED" -> Icons.Filled.Pause
                                    else -> Icons.Filled.Download
                                }
                                val statusColor = when (task.status) {
                                    "COMPLETED" -> Color(0xFF4CAF50)
                                    "FAILED" -> Color(0xFFF44336)
                                    "CANCELLED" -> Color(0xFF9E9E9E)
                                    "PAUSED" -> Color(0xFFFF9800)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.chapterTitle, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        task.status.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor
                                    )
                                }
                                if (task.status in listOf("PENDING", "DOWNLOADING")) {
                                    IconButton(onClick = { viewModel.cancelTask(task.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Cancel, "Cancel", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            if (task.status == "DOWNLOADING" && task.progress > 0f) {
                                LinearProgressIndicator(
                                    progress = { task.progress },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
