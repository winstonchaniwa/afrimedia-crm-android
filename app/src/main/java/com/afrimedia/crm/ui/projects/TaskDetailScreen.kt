package com.afrimedia.crm.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.model.ApiFailure
import com.afrimedia.crm.data.model.CommentRequest
import com.afrimedia.crm.data.model.StatusChangeRequest
import com.afrimedia.crm.data.model.Task
import com.afrimedia.crm.data.model.TaskComment
import com.afrimedia.crm.data.model.TaskRequest
import com.afrimedia.crm.data.model.TimeEntry
import com.afrimedia.crm.data.model.TimeEntryRequest
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.clients.DropdownField
import com.afrimedia.crm.ui.common.ErrorBox
import com.afrimedia.crm.ui.common.LoadingBox
import com.afrimedia.crm.ui.common.SectionTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    repository: Repository,
    taskId: Int,
    onBack: () -> Unit,
    onEditTask: (Int) -> Unit
) {
    var task by remember { mutableStateOf<Task?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var newSubtaskTitle by remember { mutableStateOf("") }
    var newCommentBody by remember { mutableStateOf("") }
    var manualMinutes by remember { mutableStateOf("") }
    var manualNote by remember { mutableStateOf("") }
    var manualDate by remember { mutableStateOf("") }

    LaunchedEffect(taskId, refreshKey) {
        isLoading = true
        when (val result = repository.call { repository.api.getTask(taskId) }) {
            is ApiResult.Success -> { task = result.data; error = null }
            is ApiResult.Failure -> error = result.error
        }
        isLoading = false
    }

    val runningEntry = task?.timeEntries?.firstOrNull { it.isRunning }
    var elapsedSeconds by remember { mutableStateOf(0L) }
    LaunchedEffect(runningEntry?.id, runningEntry?.startedAt) {
        if (runningEntry == null) {
            elapsedSeconds = 0
            return@LaunchedEffect
        }
        val startedMillis = parseServerDate(runningEntry.startedAt)
        while (true) {
            elapsedSeconds = if (startedMillis != null) {
                maxOf(0L, (System.currentTimeMillis() - startedMillis) / 1000)
            } else {
                elapsedSeconds + 1
            }
            delay(1000)
        }
    }

    fun reload() { refreshKey++ }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(task?.title ?: "Task") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = { onEditTask(taskId) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit task") }
                    IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete task") }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingBox(Modifier.padding(padding))
            error != null && task == null -> ErrorBox(error!!, onRetry = { reload() }, modifier = Modifier.padding(padding))
            else -> {
                val t = task
                if (t == null) {
                    LoadingBox(Modifier.padding(padding))
                } else {
                    Column(
                        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (t.description.isNotBlank()) Text(t.description)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DropdownField(
                                "Status",
                                listOf("to_do", "in_progress", "done"),
                                t.status,
                                Modifier.weight(1f)
                            ) { newStatus ->
                                scope.launch {
                                    repository.call { repository.api.setTaskStatus(taskId, StatusChangeRequest(newStatus)) }
                                    reload()
                                }
                            }
                            DropdownField(
                                "Priority",
                                listOf("low", "normal", "high", "urgent"),
                                t.priority,
                                Modifier.weight(1f)
                            ) { newPriority ->
                                scope.launch {
                                    repository.call {
                                        repository.api.updateTask(taskId, com.afrimedia.crm.data.model.TaskUpdateRequest(priority = newPriority))
                                    }
                                    reload()
                                }
                            }
                        }
                        Row {
                            t.assigneeName?.let { Text("Assignee: $it  ", style = MaterialTheme.typography.bodySmall) }
                            t.dueDate?.let { Text("Due: $it", style = MaterialTheme.typography.bodySmall) }
                        }

                        // Timer
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text("Time tracking", fontWeight = FontWeight.Bold)
                                Text("Total logged: ${t.totalMinutes} min")
                                if (runningEntry != null) {
                                    Text("Timer running — ${formatElapsed(elapsedSeconds)}", color = MaterialTheme.colorScheme.primary)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                                    Button(
                                        enabled = runningEntry == null && !busy,
                                        onClick = {
                                            busy = true
                                            scope.launch {
                                                repository.call { repository.api.startTaskTimer(taskId) }
                                                busy = false
                                                reload()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                        Text("Start")
                                    }
                                    Button(
                                        enabled = runningEntry != null && !busy,
                                        onClick = {
                                            busy = true
                                            scope.launch {
                                                repository.call { repository.api.stopTaskTimer(taskId) }
                                                busy = false
                                                reload()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                        Text("Stop")
                                    }
                                }
                                Divider(Modifier.padding(vertical = 8.dp))
                                Text("Log time manually", style = MaterialTheme.typography.labelLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = manualMinutes,
                                        onValueChange = { manualMinutes = it },
                                        label = { Text("Minutes") },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = manualDate,
                                        onValueChange = { manualDate = it },
                                        label = { Text("Date (YYYY-MM-DD)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                OutlinedTextField(
                                    value = manualNote,
                                    onValueChange = { manualNote = it },
                                    label = { Text("Note") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                TextButton(onClick = {
                                    val minutes = manualMinutes.toIntOrNull()
                                    if (minutes != null && minutes > 0) {
                                        scope.launch {
                                            repository.call {
                                                repository.api.addTaskTimeEntry(
                                                    taskId,
                                                    TimeEntryRequest(durationMinutes = minutes, note = manualNote, date = manualDate.ifBlank { null })
                                                )
                                            }
                                            manualMinutes = ""
                                            manualNote = ""
                                            manualDate = ""
                                            reload()
                                        }
                                    }
                                }) { Text("Add entry") }
                                if (t.timeEntries.isNotEmpty()) {
                                    Divider(Modifier.padding(vertical = 6.dp))
                                    t.timeEntries.forEach { entry -> TimeEntryRow(entry) }
                                }
                            }
                        }

                        // Subtasks
                        SectionTitle("Subtasks")
                        t.subtasks.forEach { sub ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(sub.title, modifier = Modifier.weight(1f))
                                Text(sub.status.replace('_', ' '), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newSubtaskTitle,
                                onValueChange = { newSubtaskTitle = it },
                                label = { Text("New subtask") },
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = {
                                val title = newSubtaskTitle.trim()
                                if (title.isNotEmpty()) {
                                    scope.launch {
                                        repository.call {
                                            repository.api.createTask(
                                                TaskRequest(
                                                    projectId = t.projectId,
                                                    taskListId = t.taskListId,
                                                    parentTaskId = t.id,
                                                    title = title
                                                )
                                            )
                                        }
                                        newSubtaskTitle = ""
                                        reload()
                                    }
                                }
                            }) { Text("Add") }
                        }

                        // Comments
                        SectionTitle("Comments")
                        t.comments.forEach { comment -> CommentRow(comment) }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newCommentBody,
                                onValueChange = { newCommentBody = it },
                                label = { Text("Add a comment") },
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = {
                                val body = newCommentBody.trim()
                                if (body.isNotEmpty()) {
                                    scope.launch {
                                        repository.call { repository.api.addTaskComment(taskId, CommentRequest(body)) }
                                        newCommentBody = ""
                                        reload()
                                    }
                                }
                            }) { Text("Post") }
                        }

                        error?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this task?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    scope.launch {
                        when (repository.call { repository.api.deleteTask(taskId) }) {
                            is ApiResult.Success -> onBack()
                            is ApiResult.Failure -> {}
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun TimeEntryRow(entry: TimeEntry) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(entry.userName ?: "Someone", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(
            if (entry.isRunning) "running…" else "${entry.durationMinutes} min",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CommentRow(comment: TaskComment) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(comment.userName ?: "Someone", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        Text(comment.body, style = MaterialTheme.typography.bodyMedium)
    }
}

/** started_at from the REST API is a MySQL "Y-m-d H:i:s" timestamp in UTC. */
private fun parseServerDate(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        format.parse(value)?.time
    } catch (e: Exception) {
        null
    }
}

private fun formatElapsed(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}
