package com.afrimedia.crm.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.model.ApiFailure
import com.afrimedia.crm.data.model.Client
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.common.EmptyState
import com.afrimedia.crm.ui.common.ErrorBox
import com.afrimedia.crm.ui.common.LoadingBox
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsListScreen(repository: Repository, onOpenClient: (Int) -> Unit, onNewClient: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var clients by remember { mutableStateOf<List<Client>>(emptyList()) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(query, refreshKey) {
        if (query.isNotEmpty()) delay(300) // debounce
        isLoading = true
        when (val result = repository.call { repository.api.listClients(search = query.ifBlank { null }) }) {
            is ApiResult.Success -> {
                clients = result.data
                error = null
            }
            is ApiResult.Failure -> error = result.error
        }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clients") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewClient) { Icon(Icons.Filled.Add, contentDescription = "New client") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Search clients…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            when {
                isLoading -> LoadingBox()
                error != null -> ErrorBox(error!!, onRetry = { refreshKey++ })
                clients.isEmpty() -> EmptyState("No clients yet. Tap + to add one.")
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    items(clients, key = { it.id }) { client ->
                        ClientRow(client, onClick = { onOpenClient(client.id) })
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientRow(client: Client, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(client.displayName.ifBlank { "${client.firstName} ${client.lastName}" }, fontWeight = FontWeight.Bold)
            if (client.contactPerson.isNotBlank()) {
                Text(client.contactPerson, style = MaterialTheme.typography.bodySmall)
            }
            if (client.email.isNotBlank() || client.phone.isNotBlank()) {
                Text(listOf(client.email, client.phone).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
