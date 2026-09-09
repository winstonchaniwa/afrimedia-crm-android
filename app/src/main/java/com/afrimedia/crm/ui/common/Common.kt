package com.afrimedia.crm.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.model.ApiFailure
import com.afrimedia.crm.ui.theme.AmiGreen
import com.afrimedia.crm.ui.theme.AmiOrange
import com.afrimedia.crm.ui.theme.AmiRed

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AmiOrange)
    }
}

@Composable
fun ErrorBox(error: ApiFailure, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(error.message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            if (onRetry != null) {
                androidx.compose.material3.TextButton(onClick = onRetry) { Text("Try again") }
            }
        }
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

fun statusColor(status: String) = when (status.lowercase()) {
    "paid", "accepted", "active" -> AmiGreen
    "overdue", "declined" -> AmiRed
    "draft" -> AmiOrange.copy(alpha = 0.5f)
    else -> AmiOrange
}

fun money(amount: Double, currency: String): String {
    val symbol = when (currency) {
        "USD" -> "$"
        "ZiG" -> "ZiG "
        else -> "$currency "
    }
    return symbol + String.format("%,.2f", amount)
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
}
