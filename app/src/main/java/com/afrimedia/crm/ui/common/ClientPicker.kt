package com.afrimedia.crm.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.menuAnchor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.afrimedia.crm.data.model.Client
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import kotlinx.coroutines.delay

/**
 * Type-to-search client selector. Used on the quote/invoice create/edit
 * forms — searches the /clients endpoint as the user types.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientPicker(repository: Repository, initialLabel: String, onSelected: (Client) -> Unit) {
    var query by remember { mutableStateOf(initialLabel) }
    var expanded by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Client>>(emptyList()) }

    LaunchedEffect(query) {
        if (query.length < 2) {
            results = emptyList()
            return@LaunchedEffect
        }
        delay(300)
        when (val result = repository.call { repository.api.listClients(search = query) }) {
            is ApiResult.Success -> {
                results = result.data
                expanded = result.data.isNotEmpty()
            }
            is ApiResult.Failure -> results = emptyList()
        }
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; expanded = true },
            label = { Text("Client") },
            placeholder = { Text("Search by name…") },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        androidx.compose.material3.DropdownMenu(expanded = expanded && results.isNotEmpty(), onDismissRequest = { expanded = false }) {
            results.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client.displayName.ifBlank { "${client.firstName} ${client.lastName}" }) },
                    onClick = {
                        query = client.displayName.ifBlank { "${client.firstName} ${client.lastName}" }
                        expanded = false
                        onSelected(client)
                    }
                )
            }
        }
    }
}
