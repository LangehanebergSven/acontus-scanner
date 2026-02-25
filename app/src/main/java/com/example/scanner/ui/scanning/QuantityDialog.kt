package com.example.scanner.ui.scanning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun QuantityDialog(
    itemName: String,
    initialQuantity: Int = 1,
    initialContentQuantity: Int? = null,
    confirmButtonText: String = "Hinzufügen",
    onConfirm: (Int, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val initialText = initialQuantity.toString()
    // Use TextFieldValue to handle selection
    var quantityValue by remember { 
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(0, initialText.length) // Pre-select all
            )
        ) 
    }
    
    val initialContentText = initialContentQuantity?.toString() ?: ""
    var contentQuantityValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialContentText,
                selection = TextRange(0, initialContentText.length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

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
                    value = quantityValue,
                    onValueChange = { newValue ->
                        if (newValue.text.all { char -> char.isDigit() }) {
                            quantityValue = newValue
                        }
                    },
                    label = { Text("Menge") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                quantityValue = quantityValue.copy(selection = TextRange(0, quantityValue.text.length))
                            }
                        }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = contentQuantityValue,
                    onValueChange = { newValue ->
                        if (newValue.text.all { char -> char.isDigit() }) {
                            contentQuantityValue = newValue
                        }
                    },
                    label = { Text("Inhaltsmenge (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                             val quantity = quantityValue.text.toIntOrNull() ?: 0
                            val contentQuantity = contentQuantityValue.text.toIntOrNull()
                            if (quantity > 0) {
                                onConfirm(quantity, contentQuantity)
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                contentQuantityValue = contentQuantityValue.copy(selection = TextRange(0, contentQuantityValue.text.length))
                            }
                        }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantity = quantityValue.text.toIntOrNull() ?: 0
                    val contentQuantity = contentQuantityValue.text.toIntOrNull()
                    if (quantity > 0) {
                        onConfirm(quantity, contentQuantity)
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
