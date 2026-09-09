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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.model.ApiFailure
import com.afrimedia.crm.data.model.TaskRequest
import com.afrimedia.crm.data.model.TaskUpdateRequest
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.clients.DropdownField
import com.afrimedia.crm.ui.common.ErrorBox
import com.afrimedia.crm.ui.common.LoadingBox
import kotlinx.coroutines.launch

/**
 * Create (taskId == 0, projectId/taskListId supplied by the caller — always
 * a specific task list picked on the project screen) or edit (taskId != 0)
 * a task. Status can also be set here on create; day-to-day status changes
 * happen via the quick-change menu on the project screen instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    repository: Repository,
    taskId: Int,
    projectId: Int,
    taskListId: Int,
    onBack: () -> Unit,
    onSaved: (Int) -> Unit
) {
    val isNew = taskId == 0
    var isLoading by remember { mutableStateOf(!isNew) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("to_do") }
    var priority by remember { mutableStateOf("normal") }
    var assigneeId by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    LaunchedEffect(taskId) {
        if (!isNew) {
            when (val result = repository.call { repository.api.getTask(taskId) }) {
                is ApiResult.Success -> {
                    val t = result.data
                    title = t.title
                    description = t.description
                    status = t.status
                    priority = t.priority
                    assigneeId = t.assigneeId?.toString() ?: ""
                    dueDate = t.dueDate ?: ""
                }
                is ApiResult.Failure -> error = result.error
            }
            isLoading = false
        }
    }

    fun save() {
        if (title.isBlank()) {
            error = ApiFailure("Title is required.")
            return
        }
        saving = true
        scope.launch {
            val result = if (isNew) {
                repository.call {
                    repository.api.createTask(
                        TaskRequest(
                            projectId = projectId,
                            taskListId = taskListId,
                            title = title,
                            description = description,
                            status = status,
                            priority = priority,
                            assigneeId = assigneeId.toIntOrNull(),
                            dueDate = dueDate.ifBlank { null }
                        )
                    )
                }
            } else {
                repository.call {
                    repository.api.updateTask(
                        taskId,
                        TaskUpdateRequest(
                            title = title,
                            description = description,
                            status = status,
                            priority = priority,
                            assigneeId = assigneeId.toIntOrNull(),
                            dueDate = dueDate.ifBlank { null }
                        )
                    )
                }
            }
            saving = false
            when (result) {
                is ApiResult.Success -> onSaved(result.data.id)
                is ApiResult.Failure -> error = result.error
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New task" else "Edit task") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownField("Status", listOf("to_do", "in_progress", "done"), status, Modifier.weight(1f)) { status = it }
                DropdownField("Priority", listOf("low", "normal", "high", "urgent"), priority, Modifier.weight(1f)) { priority = it }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = assigneeId,
                    onValueChange = { assigneeId = it },
                    label = { Text("Assignee user ID (optional)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
            }
            error?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { save() }, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
                Text(if (saving) "Saving…" else if (isNew) "Create task" else "Save changes")
            }
        }
    }
}
