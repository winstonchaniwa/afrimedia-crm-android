package com.afrimedia.crm.ui.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.model.ApiFailure
import com.afrimedia.crm.data.model.Project
import com.afrimedia.crm.data.model.StatusChangeRequest
import com.afrimedia.crm.data.model.Task
import com.afrimedia.crm.data.model.TaskList
import com.afrimedia.crm.data.model.TaskListRequest
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.common.ErrorBox
import com.afrimedia.crm.ui.common.LoadingBox
import com.afrimedia.crm.ui.common.SectionTitle
import com.afrimedia.crm.ui.common.money
import com.afrimedia.crm.ui.common.statusColor
import com.afrimedia.crm.ui.quotes.DocumentRow
import kotlinx.coroutines.launch

/**
 * Project header + profitability + linked documents + task lists grouped by
 * status. Mobile-appropriate: tasks are shown as a sectioned list per status
 * (tap the row's menu to move a task between statuses) rather than a
 * draggable Kanban board, per the mobile product decision.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    repository: Repository,
    projectId: Int,
    onBack: () -> Unit,
    onEditProject: (Int) -> Unit,
    onOpenTask: (Int) -> Unit,
    onNewTask: (projectId: Int, taskListId: Int) -> Unit
) {
    var project by remember { mutableStateOf<Project?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showNewListDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(projectId, refreshKey) {
        isLoading = true
        when (val result = repository.call { repository.api.getProject(projectId) }) {
            is ApiResult.Success -> { project = result.data; error = null }
            is ApiResult.Failure -> error = result.error
        }
        isLoading = false
    }

    fun changeTaskStatus(taskId: Int, status: String) {
        scope.launch {
            repository.call { repository.api.setTaskStatus(taskId, StatusChangeRequest(status)) }
            refreshKey++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: "Project") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = { onEditProject(projectId) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit project") }
                    IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete project") }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingBox(Modifier.padding(padding))
            error != null && project == null -> ErrorBox(error!!, onRetry = { refreshKey++ }, modifier = Modifier.padding(padding))
            else -> {
                val p = project
                if (p == null) {
                    LoadingBox(Modifier.padding(padding))
                } else {
                    Column(
                        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Client #${p.clientId}", style = MaterialTheme.typography.bodyMedium)
                        Text(p.status.replaceFirstChar { it.uppercase() }, color = statusColor(p.status), fontWeight = FontWeight.Bold)
                        if (p.startDate != null || p.dueDate != null) {
                            Text("${p.startDate ?: "—"}  →  ${p.dueDate ?: "—"}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (p.description.isNotBlank()) Text(p.description)

                        p.profitability?.let { ProfitabilityCard(it) }

                        p.linkedDocuments?.let { docs ->
                            if (docs.quotes.isNotEmpty() || docs.invoices.isNotEmpty()) {
                                SectionTitle("Linked documents")
                                docs.quotes.forEach { DocumentRow(it, onClick = {}) }
                                docs.invoices.forEach { DocumentRow(it, onClick = {}) }
                            }
                        }

                        SectionTitle("Task lists")
                        p.taskLists.forEach { taskList ->
                            TaskListSection(
                                taskList = taskList,
                                statuses = p.statuses,
                                onOpenTask = onOpenTask,
                                onChangeStatus = ::changeTaskStatus,
                                onAddTask = { onNewTask(projectId, taskList.id) }
                            )
                        }
                        OutlinedButton(onClick = { showNewListDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("Add task list")
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
            title = { Text("Delete this project?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    scope.launch {
                        when (repository.call { repository.api.deleteProject(projectId) }) {
                            is ApiResult.Success -> onBack()
                            is ApiResult.Failure -> {}
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    if (showNewListDialog) {
        AlertDialog(
            onDismissRequest = { showNewListDialog = false },
            title = { Text("New task list") },
            text = {
                OutlinedTextField(value = newListName, onValueChange = { newListName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newListName.trim()
                    if (name.isNotEmpty()) {
                        scope.launch {
                            repository.call { repository.api.createTaskList(projectId, TaskListRequest(name)) }
                            newListName = ""
                            showNewListDialog = false
                            refreshKey++
                        }
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showNewListDialog = false; newListName = "" }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ProfitabilityCard(p: com.afrimedia.crm.data.model.Profitability) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Profitability", fontWeight = FontWeight.Bold)
            Text("Quoted: " + money(p.revenueQuoted, p.baseCurrency))
            Text("Invoiced: " + money(p.revenueInvoiced, p.baseCurrency))
            Text("Collected: " + money(p.revenueCollected, p.baseCurrency))
            Text("Hours logged: ${p.hoursLogged}")
            if (p.hasRate) {
                Text("Cost: " + money(p.cost ?: 0.0, p.baseCurrency))
                Text(
                    "Profit: " + money(p.profit ?: 0.0, p.baseCurrency),
                    fontWeight = FontWeight.Bold,
                    color = if ((p.profit ?: 0.0) >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            } else {
                Text("Set an hourly rate to see cost/profit.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TaskListSection(
    taskList: TaskList,
    statuses: List<String>,
    onOpenTask: (Int) -> Unit,
    onChangeStatus: (Int, String) -> Unit,
    onAddTask: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(taskList.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onAddTask) { Text("+ Task") }
            }
            val grouped = taskList.tasks.groupBy { it.status }
            val statusOrder = (statuses.map { statusKey(it) } + grouped.keys).distinct()
            if (taskList.tasks.isEmpty()) {
                Text("No tasks yet.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
            }
            statusOrder.forEach { status ->
                val tasksForStatus = grouped[status].orEmpty()
                if (tasksForStatus.isNotEmpty()) {
                    Text(
                        status.replace('_', ' ').replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor(status),
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                    )
                    tasksForStatus.forEach { task ->
                        TaskRow(task, statuses, onClick = { onOpenTask(task.id) }, onChangeStatus = { onChangeStatus(task.id, it) })
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: Task, statuses: List<String>, onClick: () -> Unit, onChangeStatus: (String) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 4.dp).clickable(onClick = onClick)) {
            Text(task.title, fontWeight = FontWeight.Bold)
            Row {
                Text(task.priority.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                task.assigneeName?.let { Text("  ·  $it", style = MaterialTheme.typography.bodySmall) }
                task.dueDate?.let { Text("  ·  due $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Change status") }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                val options = if (statuses.isNotEmpty()) statuses.map { statusKey(it) } else listOf("to_do", "in_progress", "done")
                options.distinct().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.replace('_', ' ').replaceFirstChar { it.uppercase() }) },
                        onClick = { menuOpen = false; onChangeStatus(option) }
                    )
                }
            }
        }
    }
}

/** Mirrors AMCRM_Projects::status_key() on the backend (lowercase, spaces to underscores). */
fun statusKey(label: String): String = label.trim().lowercase().replace(' ', '_')
