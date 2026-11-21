package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NoteSourceType
import com.example.data.model.SavedNote
import com.example.data.model.UiState
import com.example.ui.viewmodel.FilesFilter
import com.example.ui.viewmodel.FilesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FilesScreen(
    viewModel: FilesViewModel,
    modifier: Modifier = Modifier
) {
    val notesState by viewModel.notesState.collectAsStateWithLifecycle()
    val meta by viewModel.meta.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var viewingNote by remember { mutableStateOf<SavedNote?>(null) }
    var editingNote by remember { mutableStateOf<SavedNote?>(null) }
    var showEditorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(meta.bannerMessage) {
        val msg = meta.bannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanner()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "সংরক্ষিত ফাইল ও নোট",
                        style = MaterialTheme.typography.titleLarge
                    )

                    OutlinedTextField(
                        value = meta.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("শিরোনাম, বিষয় বা নোট খুঁজুন...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "ফাইল অনুসন্ধান"
                            )
                        },
                        trailingIcon = {
                            if (meta.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "সার্চ মুছুন"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("files_search_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilesFilter.entries.forEach { filterOption ->
                            FilterChip(
                                selected = meta.filter == filterOption,
                                onClick = { viewModel.onFilterChanged(filterOption) },
                                label = { Text(filterOption.labelBn) },
                                modifier = Modifier.testTag("files_filter_${filterOption.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = notesState) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is UiState.Error -> {
                        Box(
                            Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    is UiState.Success -> {
                        val query = meta.searchQuery.trim().lowercase()
                        val filtered = state.data.filter { note ->
                            val matchesFilter = when (meta.filter) {
                                FilesFilter.ALL -> true
                                FilesFilter.AI_ONLY -> note.resolvedSourceType() == NoteSourceType.AI_ANSWER
                                FilesFilter.NOTES_ONLY -> note.resolvedSourceType() == NoteSourceType.PERSONAL_NOTE
                            }
                            val matchesQuery = query.isEmpty() ||
                                note.title.lowercase().contains(query) ||
                                note.content.lowercase().contains(query) ||
                                note.category.lowercase().contains(query)
                            matchesFilter && matchesQuery
                        }

                        if (filtered.isEmpty()) {
                            EmptyFilesState(hasAnyNotes = state.data.isNotEmpty())
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 12.dp,
                                    bottom = 92.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filtered, key = { it.id }) { note ->
                                    SavedNoteCard(
                                        note = note,
                                        onClick = { viewingNote = note },
                                        onTogglePin = { viewModel.togglePin(note.id, !note.pinned) },
                                        onEdit = {
                                            editingNote = note
                                            showEditorDialog = true
                                        },
                                        onDelete = { viewModel.deleteNote(note.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        editingNote = null
                        showEditorDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(18.dp)
                        .testTag("add_note_fab")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "নতুন ব্যক্তিগত নোট লিখুন"
                    )
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    viewingNote?.let { note ->
        NoteDetailDialog(
            note = note,
            onDismiss = { viewingNote = null },
            onEdit = {
                viewingNote = null
                editingNote = note
                showEditorDialog = true
            }
        )
    }

    if (showEditorDialog) {
        NoteEditorDialog(
            initial = editingNote,
            isSubmitting = meta.isSubmitting,
            onDismiss = { showEditorDialog = false },
            onSave = { title, content, category, pinned ->
                viewModel.saveNote(
                    existingId = editingNote?.id,
                    title = title,
                    content = content,
                    category = category,
                    pinned = pinned,
                    onComplete = { showEditorDialog = false }
                )
            }
        )
    }
}

@Composable
private fun EmptyFilesState(hasAnyNotes: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FolderCopy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = if (hasAnyNotes) "খোঁজার সাথে মিল পাওয়া যায়নি" else "এখনো কোনো ফাইল বা নোট নেই",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (hasAnyNotes) {
                        "অন্য শব্দ দিয়ে সার্চ করুন অথবা ফিল্টার পরিবর্তন করুন।"
                    } else {
                        "চ্যাট থেকে যেকোনো উত্তর 'ফাইলে সেভ করুন' অথবা নিচের '+' বাটনে নিজের নোট লিখুন।"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SavedNoteCard(
    note: SavedNote,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateLabel = (note.updatedAt ?: note.createdAt)?.toDate()?.let { dateFmt.format(it) }.orEmpty()
    val source = note.resolvedSourceType()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("note_card_${note.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (source == NoteSourceType.AI_ANSWER) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = source.labelBn,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (source == NoteSourceType.AI_ANSWER) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = note.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(48.dp).testTag("note_pin_button")
                ) {
                    Icon(
                        imageVector = if (note.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (note.pinned) "পিন সরান" else "পিন করুন",
                        tint = if (note.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp).testTag("note_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "নোট সম্পাদনা করুন"
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp).testTag("note_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "নোট মুছে ফেলুন",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteDetailDialog(
    note: SavedNote,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(note.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${note.resolvedSourceType().labelBn} • ${note.category}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onEdit) {
                Text("সম্পাদনা")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteEditorDialog(
    initial: SavedNote?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, category: String, pinned: Boolean) -> Unit
) {
    val presetCategories = listOf(
        "ব্যক্তিগত নোট",
        "মেডিকেল সার্জিক্যাল",
        "ফার্মাকোলজি",
        "জরুরি সেবা",
        "নার্সিং কেয়ার প্ল্যান"
    )
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var content by remember { mutableStateOf(initial?.content.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category ?: presetCategories.first()) }
    var pinned by remember { mutableStateOf(initial?.pinned ?: false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "নতুন নোট লিখুন" else "নোট সম্পাদনা করুন") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("শিরোনাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                )

                Text("ক্যাটাগরি:", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetCategories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("নোটের বিস্তারিত *") },
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth().testTag("note_content_input")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { pinned = !pinned }
                ) {
                    Checkbox(checked = pinned, onCheckedChange = { pinned = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("উপরে পিন করে রাখুন", style = MaterialTheme.typography.bodyMedium)
                }

                if (!validationError.isNullOrBlank()) {
                    Text(
                        text = validationError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        validationError = "শিরোনাম ও বিস্তারিত তথ্য লিখুন।"
                    } else {
                        validationError = null
                        onSave(title, content, category, pinned)
                    }
                },
                modifier = Modifier.testTag("note_save_button")
            ) {
                Text(if (isSubmitting) "সেভ হচ্ছে..." else "সেভ করুন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
