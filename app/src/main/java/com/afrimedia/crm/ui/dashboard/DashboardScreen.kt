package com.afrimedia.crm.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.afrimedia.crm.data.model.Dashboard
import com.afrimedia.crm.data.model.Me
import com.afrimedia.crm.data.repo.ApiResult
import com.afrimedia.crm.data.repo.Repository
import com.afrimedia.crm.ui.common.ErrorBox
import com.afrimedia.crm.ui.common.LoadingBox
import com.afrimedia.crm.ui.common.money
import com.afrimedia.crm.ui.theme.AmiGreen
import com.afrimedia.crm.ui.theme.AmiOrange
import com.afrimedia.crm.ui.theme.AmiRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(repository: Repository) {
    var me by remember { mutableStateOf<Me?>(null) }
    var dashboard by remember { mutableStateOf<Dashboard?>(null) }
    var error by remember { mutableStateOf<ApiFailure?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        isLoading = true
        error = null
        val meResult = repository.call { repository.api.getMe() }
        val dashResult = repository.call { repository.api.getDashboard() }
        when (meResult) {
            is ApiResult.Success -> me = meResult.data
            is ApiResult.Failure -> error = meResult.error
        }
        when (dashResult) {
            is ApiResult.Success -> dashboard = dashResult.data
            is ApiResult.Failure -> if (error == null) error = dashResult.error
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard") })
        }
    ) { padding ->
        when {
            isLoading -> LoadingBox(Modifier.padding(padding))
            error != null -> ErrorBox(error!!, onRetry = { refreshKey++ }, modifier = Modifier.padding(padding))
            else -> {
                val d = dashboard
                val currentUser = me
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        currentUser?.let {
                            Text(
                                "Hi ${it.name.ifBlank { "there" }}" + (it.branchName?.let { b -> " — $b" } ?: ""),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (d != null) {
                        item { StatCard("Invoiced this month", money(d.invoiced, d.baseCurrency), AmiOrange) }
                        item { StatCard("Revenue collected", money(d.collected, d.baseCurrency), AmiGreen) }
                        item { StatCard("Outstanding", money(d.outstanding, d.baseCurrency), AmiRed) }
                        if (d.branches.isNotEmpty()) {
                            item {
                                Text("By branch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            }
                            items(d.branches) { b ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(b.branchName, fontWeight = FontWeight.Bold)
                                        Text("Invoiced ${money(b.invoiced, d.baseCurrency)} · Collected ${money(b.collected, d.baseCurrency)} · Outstanding ${money(b.outstanding, d.baseCurrency)}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, accent: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}
