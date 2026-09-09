package com.afrimedia.crm.ui.projects

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.model.ApiFailure
import com.afrimedia.crm.data.model.Project
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.common.EmptyState
import com.afrimedia.crm.ui.common.ErrorBox
import com.afrimedia.crm.ui.common.LoadingBox
import com.afrimedia.crm.ui.common.statusColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(repository: Repository, onOpenProject: (Int) -> Unit, onNewProject: () -> Unit) {
    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        isLoading = true
        when (val result = repository.call { repository.api.listProjects() }) {
            is ApiResult.Success -> { projects = result.data; error = null }
            is ApiResult.Failure -> error = result.error
        }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Projects") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewProject) { Icon(Icons.Filled.Add, contentDescription = "New project") }
        }
    ) { padding ->
        when {
            isLoading -> LoadingBox(Modifier.padding(padding))
            error != null -> ErrorBox(error!!, onRetry = { refreshKey++ }, modifier = Modifier.padding(padding))
            projects.isEmpty() -> EmptyState("No projects yet. Tap + to create one.", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    ProjectRow(project, onClick = { onOpenProject(project.id) })
                    Divider()
                }
            }
        }
    }
}

/**
 * project_payload() doesn't include a client_name (only client_id) for the
 * list endpoint, so we show the client id here rather than firing an extra
 * per-row lookup call. The full client name is resolved on the project
 * detail screen instead, where a single ClientPicker-style lookup is cheap.
 */
@Composable
fun ProjectRow(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(project.name, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Client #${project.clientId}", style = MaterialTheme.typography.bodyMedium)
                project.branchId?.let {
                    Text("  ·  Branch #$it", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                project.status.replaceFirstChar { it.uppercase() },
                color = statusColor(project.status),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
