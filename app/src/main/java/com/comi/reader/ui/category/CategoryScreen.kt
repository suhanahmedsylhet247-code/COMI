package com.comi.reader.ui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.comi.reader.domain.model.Category

@Composable
fun CategoryScreen(
    onCategoryClick: (Long) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, "Add category")
            }
        }
    ) { padding ->
        if (categories.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No categories yet", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Tap + to create a category", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.id }) { category ->
                    Card(
                        onClick = { onCategoryClick(category.id) },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            }
                            IconButton(onClick = { editingCategory = category }) {
                                Icon(Icons.Filled.Edit, "Edit")
                            }
                            IconButton(onClick = { viewModel.deleteCategory(category.id) }) {
                                Icon(Icons.Filled.Delete, "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CategoryDialog(
            title = "New Category",
            initialName = "",
            onConfirm = { name -> viewModel.createCategory(name); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    editingCategory?.let { category ->
        CategoryDialog(
            title = "Edit Category",
            initialName = category.name,
            onConfirm = { name -> viewModel.updateCategory(category.copy(name = name)); editingCategory = null },
            onDismiss = { editingCategory = null }
        )
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
