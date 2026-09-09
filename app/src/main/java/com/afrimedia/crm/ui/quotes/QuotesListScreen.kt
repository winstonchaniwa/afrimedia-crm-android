package com.afrimedia.crm.ui.quotes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.afrimedia.crm.data.model.Document
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.common.EmptyState
import com.afrimedia.crm.ui.common.ErrorBox
import com.afrimedia.crm.ui.common.LoadingBox
import com.afrimedia.crm.ui.common.money
import com.afrimedia.crm.ui.common.statusColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesListScreen(repository: Repository, onOpenQuote: (Int) -> Unit, onNewQuote: () -> Unit) {
    var quotes by remember { mutableStateOf<List<Document>>(emptyList()) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        isLoading = true
        when (val result = repository.call { repository.api.listQuotes() }) {
            is ApiResult.Success -> { quotes = result.data; error = null }
            is ApiResult.Failure -> error = result.error
        }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Quotes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewQuote) { Icon(Icons.Filled.Add, contentDescription = "New quote") }
        }
    ) { padding ->
        when {
            isLoading -> LoadingBox(Modifier.padding(padding))
            error != null -> ErrorBox(error!!, onRetry = { refreshKey++ }, modifier = Modifier.padding(padding))
            quotes.isEmpty() -> EmptyState("No quotes yet. Tap + to create one.", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp)
            ) {
                items(quotes, key = { it.id }) { quote ->
                    DocumentRow(quote, onClick = { onOpenQuote(quote.id) })
                    Divider()
                }
            }
        }
    }
}

@Composable
fun DocumentRow(doc: Document, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(doc.number, fontWeight = FontWeight.Bold)
            Text(doc.clientName, style = MaterialTheme.typography.bodyMedium)
            Column {
                Text(money(doc.total, doc.currency), fontWeight = FontWeight.Bold)
                Text(doc.status.replaceFirstChar { it.uppercase() }, color = statusColor(doc.status), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
