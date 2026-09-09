package com.afrimedia.crm.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.model.ApiFailure
import com.afrimedia.crm.data.model.Client
import com.afrimedia.crm.data.model.ClientRequest
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.common.ErrorBox
import com.afrimedia.crm.ui.common.LoadingBox
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(repository: Repository, clientId: Int, onBack: () -> Unit) {
    val isNew = clientId == 0
    var isLoading by remember { mutableStateOf(!isNew) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var saving by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var clientType by remember { mutableStateOf("individual") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("active") }

    LaunchedEffect(clientId) {
        if (!isNew) {
            when (val result = repository.call { repository.api.getClient(clientId) }) {
                is ApiResult.Success -> {
                    val c = result.data
                    clientType = c.clientType
                    firstName = c.firstName
                    lastName = c.lastName
                    company = c.company
                    email = c.email
                    phone = c.phone
                    contactPerson = c.contactPerson
                    contactPhone = c.contactPhone
                    addressLine1 = c.addressLine1
                    city = c.city
                    country = c.country
                    notes = c.notes
                    status = c.status
                }
                is ApiResult.Failure -> error = result.error
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New client" else "Edit client") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingBox(Modifier.padding(padding))
            error != null -> ErrorBox(error!!, modifier = Modifier.padding(padding))
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DropdownField("Client type", listOf("individual", "business"), clientType) { clientType = it }
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Business name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contactPerson, onValueChange = { contactPerson = it }, label = { Text("Contact person") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Contact phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = addressLine1, onValueChange = { addressLine1 = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, modifier = Modifier.fillMaxWidth())
                DropdownField("Status", listOf("active", "inactive"), status) { status = it }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                error?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = {
                        saving = true
                        val body = ClientRequest(
                            clientType = clientType,
                            firstName = firstName,
                            lastName = lastName,
                            company = company,
                            email = email,
                            phone = phone,
                            contactPerson = contactPerson,
                            contactPhone = contactPhone,
                            addressLine1 = addressLine1,
                            city = city,
                            country = country,
                            notes = notes,
                            status = status
                        )
                        scope.launch {
                            val result = if (isNew) {
                                repository.call { repository.api.createClient(body) }
                            } else {
                                repository.call { repository.api.updateClient(clientId, body) }
                            }
                            saving = false
                            when (result) {
                                is ApiResult.Success -> onBack()
                                is ApiResult.Failure -> error = result.error
                            }
                        }
                    },
                    enabled = !saving && (firstName.isNotBlank() || company.isNotBlank()),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(if (saving) "Saving…" else if (isNew) "Create client" else "Save changes")
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this client?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    scope.launch {
                        when (repository.call { repository.api.deleteClient(clientId) }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, options: List<String>, selected: String, modifier: Modifier = Modifier, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected.replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.replaceFirstChar { it.uppercase() }) }, onClick = {
                    onSelect(option)
                    expanded = false
                })
            }
        }
    }
}
