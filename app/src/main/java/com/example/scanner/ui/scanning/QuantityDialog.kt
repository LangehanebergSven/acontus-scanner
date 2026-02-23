package com.example.scanner.ui.scanning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun QuantityDialog(
    itemName: String,
    initialQuantity: Int = 1,
    confirmButtonText: String = "Hinzufügen",
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Use TextFieldValue to handle selection/cursor position if needed, 
    // but String is enough for simple input. 
    // Using String state to pre-fill initial quantity.
    var quantityText by remember { mutableStateOf(initialQuantity.toString()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = itemName)
        },
        text = {
            Column {
                Text(text = "Menge eingeben:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            quantityText = it
                        }
                    },
                    label = { Text("Menge") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantity = quantityText.toIntOrNull() ?: 0
                    if (quantity > 0) {
                        onConfirm(quantity)
                    }
                }
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
