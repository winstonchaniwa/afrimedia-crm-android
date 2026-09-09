package com.afrimedia.crm.ui.invoices

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
import androidx.compose.material3.AlertDialog
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
import com.afrimedia.crm.data.model.Document
import com.afrimedia.crm.data.model.DocumentRequest
import com.afrimedia.crm.data.model.LineItemRequest
import com.afrimedia.crm.data.model.PaymentRequest
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
fun InvoiceDetailScreen(repository: Repository, invoiceId: Int, onBack: () -> Unit) {
    val isNew = invoiceId == 0
    var isLoading by remember { mutableStateOf(!isNew) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var saving by remember { mutableStateOf(false) }
    var invoice by remember { mutableStateOf<Document?>(null) }
    var editing by remember { mutableStateOf(isNew) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var clientId by remember { mutableStateOf(0) }
    var clientLabel by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var discountValue by remember { mutableStateOf("0") }
    var taxRate by remember { mutableStateOf("0") }
    var issueDate by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf(LineItemRequest(description = "", qty = 1.0, unitPrice = 0.0))) }

    LaunchedEffect(invoiceId) {
        if (!isNew) {
            when (val result = repository.call { repository.api.getInvoice(invoiceId) }) {
                is ApiResult.Success -> {
                    val inv = result.data
                    invoice = inv
                    clientId = inv.clientId
                    clientLabel = inv.clientName.ifBlank { inv.client?.displayName ?: "" }
                    currency = inv.currency
                    discountValue = inv.discountValue.toString()
                    taxRate = inv.taxRate.toString()
                    issueDate = inv.issueDate
                    dueDate = inv.dueDate ?: ""
                    notes = inv.notes
                    terms = inv.terms
                    items = inv.items.map { LineItemRequest(it.description, it.qty, it.unitPrice, it.taxRate) }
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
            dueDate = dueDate.ifBlank { null },
            notes = notes,
            terms = terms,
            items = items.filter { it.description.isNotBlank() }
        )
        scope.launch {
            val result = if (isNew) repository.call { repository.api.createInvoice(body) }
            else repository.call { repository.api.updateInvoice(invoiceId, body) }
            saving = false
            when (result) {
                is ApiResult.Success -> {
                    if (isNew) onBack() else { invoice = result.data; editing = false }
                }
                is ApiResult.Failure -> error = result.error
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New invoice" else invoice?.number ?: "Invoice") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingBox(Modifier.padding(padding))
            error != null && invoice == null && !isNew -> ErrorBox(error!!, modifier = Modifier.padding(padding))
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
                    OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = discountValue, onValueChange = { discountValue = it }, label = { Text("Discount amount") }, modifier = Modifier.fillMaxWidth())
                LineItemsEditor(items) { items = it }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = terms, onValueChange = { terms = it }, label = { Text("Terms / bank details") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                error?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isNew) OutlinedButton(onClick = { editing = false }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = { save() }, enabled = !saving, modifier = Modifier.weight(1f)) {
                        Text(if (saving) "Saving…" else "Save")
                    }
                }
            }
            else -> {
                val inv = invoice
                if (inv == null) {
                    LoadingBox(Modifier.padding(padding))
                } else {
                    Column(
                        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(inv.clientName.ifBlank { inv.client?.displayName ?: "" }, style = MaterialTheme.typography.titleMedium)
                        Text(inv.status.replaceFirstChar { it.uppercase() }, color = statusColor(inv.status), fontWeight = FontWeight.Bold)
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                inv.items.forEach { item ->
                                    Row(Modifier.fillMaxWidth()) {
                                        Text(item.description, Modifier.weight(2f))
                                        Text("${item.qty}", Modifier.weight(0.5f))
                                        Text(money(item.lineTotal, inv.currency), Modifier.weight(1f))
                                    }
                                }
                                Text("Total: " + money(inv.total, inv.currency), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                                Text("Paid: " + money(inv.amountPaid ?: 0.0, inv.currency))
                                Text(
                                    "Balance due: " + money(inv.balanceDue ?: (inv.total - (inv.amountPaid ?: 0.0)), inv.currency),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (inv.payments.isNotEmpty()) {
                            Text("Payments", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            inv.payments.forEach { p ->
                                Text("${p.paidOn} · ${money(p.amount, p.currency)} · ${p.method.ifBlank { "—" }}")
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { editing = true }, modifier = Modifier.weight(1f)) { Text("Edit") }
                            if ((inv.balanceDue ?: 0.0) > 0.005) {
                                Button(onClick = { showPaymentDialog = true }, modifier = Modifier.weight(1f)) { Text("Record payment") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        RecordPaymentDialog(
            defaultCurrency = invoice?.currency ?: "USD",
            onDismiss = { showPaymentDialog = false },
            onSubmit = { amount, method, reference, paidOn ->
                scope.launch {
                    val body = PaymentRequest(amount = amount, currency = invoice?.currency ?: "USD", method = method, reference = reference, paidOn = paidOn)
                    when (val result = repository.call { repository.api.recordPayment(invoiceId, body) }) {
                        is ApiResult.Success -> { invoice = result.data; showPaymentDialog = false }
                        is ApiResult.Failure -> { error = result.error; showPaymentDialog = false }
                    }
                }
            }
        )
    }
}

@Composable
private fun RecordPaymentDialog(
    defaultCurrency: String,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, method: String, reference: String, paidOn: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var paidOn by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record payment ($defaultCurrency)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = method, onValueChange = { method = it }, label = { Text("Method (cash, EcoCash, bank…)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = reference, onValueChange = { reference = it }, label = { Text("Reference") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = paidOn, onValueChange = { paidOn = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = amount.toDoubleOrNull()
                if (value != null && value > 0) onSubmit(value, method, reference, paidOn)
            }) { Text("Record") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
