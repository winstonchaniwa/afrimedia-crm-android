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
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.model.ApiFailure
import com.afrimedia.crm.data.model.ProjectRequest
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.clients.DropdownField
import com.afrimedia.crm.ui.common.ClientPicker
import com.afrimedia.crm.ui.common.ErrorBox
import com.afrimedia.crm.ui.common.LoadingBox
import kotlinx.coroutines.launch

/** Create/edit form for a project. Reuses ClientPicker and the DropdownField helper from the client form. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    repository: Repository,
    projectId: Int,
    onBack: () -> Unit,
    onSaved: (Int) -> Unit
) {
    val isNew = projectId == 0
    var isLoading by remember { mutableStateOf(!isNew) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var clientId by remember { mutableStateOf(0) }
    var clientLabel by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("active") }
    var startDate by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    // Newline-separated custom Kanban column labels, same convention as the web admin's textarea.
    var statusesText by remember { mutableStateOf("") }

    LaunchedEffect(projectId) {
        if (!isNew) {
            when (val result = repository.call { repository.api.getProject(projectId) }) {
                is ApiResult.Success -> {
                    val p = result.data
                    clientId = p.clientId
                    clientLabel = "Client #${p.clientId}"
                    name = p.name
                    description = p.description
                    hourlyRate = p.hourlyRate?.toString() ?: ""
                    status = p.status
                    startDate = p.startDate ?: ""
                    dueDate = p.dueDate ?: ""
                    statusesText = p.statuses.joinToString("\n")
                }
                is ApiResult.Failure -> error = result.error
            }
            isLoading = false
        }
    }

    fun save() {
        if (clientId == 0) {
            error = ApiFailure("Choose a client first.")
            return
        }
        if (name.isBlank()) {
            error = ApiFailure("Project name is required.")
            return
        }
        saving = true
        val statusList = statusesText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val body = ProjectRequest(
            clientId = clientId,
            name = name,
            description = description,
            statuses = statusList.ifEmpty { null },
            hourlyRate = hourlyRate.toDoubleOrNull(),
            status = status,
            startDate = startDate.ifBlank { null },
            dueDate = dueDate.ifBlank { null }
        )
        scope.launch {
            val result = if (isNew) repository.call { repository.api.createProject(body) }
            else repository.call { repository.api.updateProject(projectId, body) }
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
                title = { Text(if (isNew) "New project" else "Edit project") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        if (error != null && isNew.not() && name.isBlank()) {
            ErrorBox(error!!, modifier = Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClientPicker(repository, clientLabel) { client -> clientId = client.id; clientLabel = client.displayName }
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Project name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = hourlyRate, onValueChange = { hourlyRate = it }, label = { Text("Hourly rate (optional)") }, modifier = Modifier.weight(1f))
                DropdownField("Status", listOf("active", "on_hold", "completed", "cancelled"), status, Modifier.weight(1f)) { status = it }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(
                value = statusesText,
                onValueChange = { statusesText = it },
                label = { Text("Custom board columns (one per line, optional)") },
                placeholder = { Text("To Do\nIn Progress\nDone") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            error?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { save() }, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
                Text(if (saving) "Saving…" else if (isNew) "Create project" else "Save changes")
            }
        }
    }
}
