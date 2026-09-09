package com.afrimedia.crm.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.model.LineItemRequest

/** Editable line-item table shared by the quote and invoice create/edit screens. */
@Composable
fun LineItemsEditor(items: List<LineItemRequest>, onChange: (List<LineItemRequest>) -> Unit) {
    Column {
        Text("Line items", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item.description,
                    onValueChange = { newDesc ->
                        onChange(items.toMutableList().also { it[index] = item.copy(description = newDesc) })
                    },
                    label = { Text("Description") },
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = if (item.qty == item.qty.toLong().toDouble()) item.qty.toLong().toString() else item.qty.toString(),
                    onValueChange = { newQty ->
                        onChange(items.toMutableList().also { it[index] = item.copy(qty = newQty.toDoubleOrNull() ?: item.qty) })
                    },
                    label = { Text("Qty") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                )
                OutlinedTextField(
                    value = if (item.unitPrice == item.unitPrice.toLong().toDouble()) item.unitPrice.toLong().toString() else item.unitPrice.toString(),
                    onValueChange = { newPrice ->
                        onChange(items.toMutableList().also { it[index] = item.copy(unitPrice = newPrice.toDoubleOrNull() ?: item.unitPrice) })
                    },
                    label = { Text("Price") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                )
                IconButton(onClick = {
                    onChange(items.toMutableList().also { it.removeAt(index) })
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove line")
                }
            }
        }
        TextButton(onClick = {
            onChange(items + LineItemRequest(description = "", qty = 1.0, unitPrice = 0.0))
        }) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            Text("Add line")
        }
        val subtotal = items.sumOf { it.qty * it.unitPrice }
        Text("Subtotal: " + String.format("%,.2f", subtotal), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
    }
}
