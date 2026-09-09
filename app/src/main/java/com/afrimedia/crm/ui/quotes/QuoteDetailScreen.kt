package com.afrimedia.crm.ui.quotes

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.model.ApiFailure
import com.afrimedia.crm.data.model.Document
import com.afrimedia.crm.data.model.DocumentRequest
import com.afrimedia.crm.data.model.LineItemRequest
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.common.ClientPicker
import com.afrimedia.crm.ui.common.ErrorBox
import com.afrimedia.crm.ui.common.LineItemsEditor
import com.afrimedia.crm.ui.common.LoadingBox
import com.afrimedia.crm.ui.common.money
import com.afrimedia.crm.ui.common.statusColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    repository: Repository,
    quoteId: Int,
    onBack: () -> Unit,
    onOpenInvoice: (Int) -> Unit
) {
    val isNew = quoteId == 0
    var isLoading by remember { mutableStateOf(!isNew) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var saving by remember { mutableStateOf(false) }
    var quote by remember { mutableStateOf<Document?>(null) }
    var editing by remember { mutableStateOf(isNew) }
    val scope = rememberCoroutineScope()

    var clientId by remember { mutableStateOf(0) }
    var clientLabel by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var discountValue by remember { mutableStateOf("0") }
    var taxRate by remember { mutableStateOf("0") }
    var issueDate by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf(LineItemRequest(description = "", qty = 1.0, unitPrice = 0.0))) }

    LaunchedEffect(quoteId) {
        if (!isNew) {
            when (val result = repository.call { repository.api.getQuote(quoteId) }) {
                is ApiResult.Success -> {
                    val q = result.data
                    quote = q
                    clientId = q.clientId
                    clientLabel = q.clientName.ifBlank { q.client?.displayName ?: "" }
                    currency = q.currency
                    discountValue = q.discountValue.toString()
                    taxRate = q.taxRate.toString()
                    issueDate = q.issueDate
                    expiryDate = q.expiryDate ?: ""
                    notes = q.notes
                    terms = q.terms
                    items = q.items.map { LineItemRequest(it.description, it.qty, it.unitPrice, it.taxRate) }
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
        saving = true
        val body = DocumentRequest(
            clientId = clientId,
            currency = currency,
            discountValue = discountValue.toDoubleOrNull() ?: 0.0,
            taxRate = taxRate.toDoubleOrNull() ?: 0.0,
            issueDate = issueDate.ifBlank { java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date()) },
            expiryDate = expiryDate.ifBlank { null },
            notes = notes,
            terms = terms,
            items = items.filter { it.description.isNotBlank() }
        )
        scope.launch {
            val result = if (isNew) repository.call { repository.api.createQuote(body) }
            else repository.call { repository.api.updateQuote(quoteId, body) }
            saving = false
            when (result) {
                is ApiResult.Success -> {
                    quote = result.data
                    editing = false
                    if (isNew) onBack() else editing = false
                }
                is ApiResult.Failure -> error = result.error
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New quote" else quote?.number ?: "Quote") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingBox(Modifier.padding(padding))
            error != null && quote == null && !isNew -> ErrorBox(error!!, modifier = Modifier.padding(padding))
            editing -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClientPicker(repository, clientLabel) { client -> clientId = client.id; clientLabel = client.displayName }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = currency, onValueChange = { currency = it }, label = { Text("Currency") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = taxRate, onValueChange = { taxRate = it }, label = { Text("Tax %") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = issueDate, onValueChange = { issueDate = it }, label = { Text("Issue date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("Valid until (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = discountValue, onValueChange = { discountValue = it }, label = { Text("Discount amount") }, modifier = Modifier.fillMaxWidth())
                LineItemsEditor(items) { items = it }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = terms, onValueChange = { terms = it }, label = { Text("Terms") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                error?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isNew) OutlinedButton(onClick = { editing = false }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { save() }, enabled = !saving, modifier = Modifier.weight(1f)) {
                        Text(if (saving) "Saving…" else "Save")
                    }
                }
            }
            else -> {
                val q = quote
                if (q == null) {
                    LoadingBox(Modifier.padding(padding))
                } else {
                    Column(
                        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(q.clientName.ifBlank { q.client?.displayName ?: "" }, style = MaterialTheme.typography.titleMedium)
                        Text(q.status.replaceFirstChar { it.uppercase() }, color = statusColor(q.status), fontWeight = FontWeight.Bold)
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                q.items.forEach { item ->
                                    Row(Modifier.fillMaxWidth()) {
                                        Text(item.description, Modifier.weight(2f))
                                        Text("${item.qty}", Modifier.weight(0.5f))
                                        Text(money(item.lineTotal, q.currency), Modifier.weight(1f))
                                    }
                                }
                                Text("Total: " + money(q.total, q.currency), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                        if (q.notes.isNotBlank()) Text("Notes: ${q.notes}")
                        if (q.terms.isNotBlank()) Text("Terms: ${q.terms}")

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { editing = true }, modifier = Modifier.weight(1f)) { Text("Edit") }
                            if (q.status != "converted") {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            when (val result = repository.call { repository.api.convertQuote(quoteId) }) {
                                                is ApiResult.Success -> onOpenInvoice(result.data.id)
                                                is ApiResult.Failure -> error = result.error
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Convert to invoice") }
                            }
                        }
                    }
                }
            }
        }
    }
}
