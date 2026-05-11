package com.comi.reader.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.comi.reader.domain.model.ReadingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    defaultReadingMode: ReadingMode,
    onSetDefaultReadingMode: (ReadingMode) -> Unit
) {
    var showReadingModeOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance section
            SectionHeader("Appearance")
            SettingsRow(
                icon = { Icon(Icons.Filled.DarkMode, contentDescription = null) },
                title = "Dark Mode",
                subtitle = "Use dark theme",
                trailing = {
                    Switch(checked = isDarkMode, onCheckedChange = onToggleDarkMode)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Reading section
            SectionHeader("Reading")
            SettingsRow(
                icon = { Icon(Icons.Filled.MenuBook, contentDescription = null) },
                title = "Default Reading Mode",
                subtitle = when (defaultReadingMode) {
                    ReadingMode.LEFT_TO_RIGHT -> "Left to Right"
                    ReadingMode.RIGHT_TO_LEFT -> "Right to Left (Manga)"
                    ReadingMode.VERTICAL -> "Vertical"
                    ReadingMode.WEBTOON -> "Webtoon (Continuous)"
                },
                onClick = { showReadingModeOptions = !showReadingModeOptions }
            )
            if (showReadingModeOptions) {
                ReadingMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSetDefaultReadingMode(mode) }
                            .padding(start = 56.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == defaultReadingMode,
                            onClick = { onSetDefaultReadingMode(mode) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when (mode) {
                                ReadingMode.LEFT_TO_RIGHT -> "Left to Right"
                                ReadingMode.RIGHT_TO_LEFT -> "Right to Left (Manga)"
                                ReadingMode.VERTICAL -> "Vertical"
                                ReadingMode.WEBTOON -> "Webtoon (Continuous)"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Storage section
            SectionHeader("Storage")
            SettingsRow(
                icon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                title = "Storage Location",
                subtitle = "Internal storage"
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Extensions section
            SectionHeader("Extensions")
            SettingsRow(
                icon = { Icon(Icons.Filled.Extension, contentDescription = null) },
                title = "Manage Extensions",
                subtitle = "Add or remove manga sources"
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // About section
            SectionHeader("About")
            SettingsRow(
                icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                title = "COMI",
                subtitle = "Version 1.0.0 · Open Source Comic Reader"
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing?.invoke()
    }
}
