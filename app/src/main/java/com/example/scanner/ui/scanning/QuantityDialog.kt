package com.example.scanner.ui.scanning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityDialog(
    itemName: String,
    initialQuantity: Int = 1,
    showMhdField: Boolean = false,
    initialMhd: Date? = null,
    confirmButtonText: String = "Hinzufügen",
    onConfirm: (Int, Date?) -> Unit,
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

    var selectedMhd by remember { mutableStateOf(initialMhd) }
    var showDatePicker by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    val quantity = quantityValue.text.toIntOrNull() ?: 0
    val isMhdValid = !showMhdField || selectedMhd != null
    val isValid = quantity >= 0 && isMhdValid // Updated to allow 0

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedMhd?.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedMhd = Date(it + TimeZone.getDefault().getOffset(it))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Abbrechen") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValid) {
                                onConfirm(quantity, selectedMhd)
                            }
                        }
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

                if (showMhdField) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val mhdFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY) }
                    OutlinedTextField(
                        value = selectedMhd?.let { mhdFormatter.format(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("MHD") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(quantity, selectedMhd)
                    }
                },
                enabled = isValid
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
