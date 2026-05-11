package com.comi.reader.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.comi.reader.domain.model.ReadingMode
import com.comi.reader.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToBackup: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Appearance
        SettingsSection("Appearance") {
            // Theme mode
            var showThemeMenu by remember { mutableStateOf(false) }
            SettingsItem(
                icon = Icons.Filled.DarkMode,
                title = "Theme",
                subtitle = state.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { showThemeMenu = true }
            ) {
                DropdownMenu(expanded = showThemeMenu, onDismissRequest = { showThemeMenu = false }) {
                    ThemeMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = { viewModel.setThemeMode(mode); showThemeMenu = false }
                        )
                    }
                }
            }

            SettingsToggle(Icons.Filled.DarkMode, "AMOLED Dark", "Pure black background in dark mode", state.amoledDark) {
                viewModel.setAmoledDark(it)
            }

            SettingsToggle(Icons.Filled.Palette, "Dynamic Colors", "Material You color scheme (Android 12+)", state.dynamicColors) {
                viewModel.setDynamicColors(it)
            }
        }

        // Reader
        SettingsSection("Reader") {
            var showModeMenu by remember { mutableStateOf(false) }
            SettingsItem(
                icon = Icons.Filled.MenuBook,
                title = "Default Reading Mode",
                subtitle = state.defaultReadingMode.name.replace('_', ' '),
                onClick = { showModeMenu = true }
            ) {
                DropdownMenu(expanded = showModeMenu, onDismissRequest = { showModeMenu = false }) {
                    ReadingMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name.replace('_', ' ')) },
                            onClick = { viewModel.setDefaultReadingMode(mode); showModeMenu = false }
                        )
                    }
                }
            }

            SettingsToggle(null, "Show Page Number", null, state.showPageNumber) {
                viewModel.setShowPageNumber(it)
            }
            SettingsToggle(null, "Keep Screen On", null, state.keepScreenOn) {
                viewModel.setKeepScreenOn(it)
            }
            SettingsToggle(null, "Fullscreen Reader", null, state.fullscreen) {
                viewModel.setFullscreen(it)
            }
            SettingsToggle(null, "Page Transitions", "Animate when changing pages", state.animateTransitions) {
                viewModel.setAnimateTransitions(it)
            }
            SettingsToggle(null, "Double-Tap Zoom", null, state.doubleTapZoom) {
                viewModel.setDoubleTapZoom(it)
            }
            SettingsToggle(null, "Double Page Mode", "Show two pages side-by-side", state.doublePageMode) {
                viewModel.setDoublePageMode(it)
            }
        }

        // Downloads
        SettingsSection("Downloads") {
            SettingsToggle(Icons.Filled.Wifi, "Wi-Fi Only", "Only download on Wi-Fi", state.downloadWifiOnly) {
                viewModel.setDownloadWifiOnly(it)
            }
            SettingsItem(icon = Icons.Filled.Download, title = "Download Queue", subtitle = "View and manage downloads", onClick = onNavigateToDownloads)
        }

        // Library
        SettingsSection("Library") {
            SettingsItem(icon = Icons.Filled.Category, title = "Categories", subtitle = "Manage library categories", onClick = onNavigateToCategories)
        }

        // Privacy
        SettingsSection("Privacy & Security") {
            SettingsToggle(Icons.Filled.VisibilityOff, "Incognito Mode", "Don't save reading history", state.incognitoMode) {
                viewModel.setIncognitoMode(it)
            }
            SettingsToggle(Icons.Filled.Security, "App Lock", "Require PIN to open app", state.appLockEnabled) {
                viewModel.setAppLockEnabled(it)
            }
        }

        // Backup
        SettingsSection("Backup & Restore") {
            SettingsItem(icon = Icons.Filled.Backup, title = "Backup & Restore", subtitle = "Export and import library data", onClick = onNavigateToBackup)
            SettingsToggle(Icons.Filled.Sync, "Auto Backup", "Automatically back up library", state.autoBackupEnabled) {
                viewModel.setAutoBackupEnabled(it)
            }
        }

        // Updates
        SettingsSection("Updates") {
            SettingsToggle(Icons.Filled.Notifications, "Check for Updates", "Check for new chapters automatically", state.checkUpdates) {
                viewModel.setCheckUpdates(it)
            }
        }

        // Statistics
        SettingsSection("Data") {
            SettingsItem(icon = Icons.Filled.BarChart, title = "Statistics", subtitle = "View reading statistics", onClick = onNavigateToStatistics)
            SettingsItem(icon = Icons.Filled.Storage, title = "Storage", subtitle = "Internal storage")
        }

        // About
        SettingsSection("About") {
            SettingsItem(icon = Icons.Filled.Info, title = "COMI Reader", subtitle = "Version 1.0.0 · Kotlin + Jetpack Compose")
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(4.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector?,
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector?,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onChanged: (Boolean) -> Unit
) {
    SettingsItem(icon = icon, title = title, subtitle = subtitle) {
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
