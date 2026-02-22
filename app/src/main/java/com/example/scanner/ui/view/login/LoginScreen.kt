package com.example.scanner.ui.view.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.scanner.ui.theme.ScannerTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var personalNr by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bitte anmelden")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = personalNr,
                onValueChange = { personalNr = it },
                label = { Text("Personal-Nr.") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                // Dummy validation
                if (personalNr.isNotEmpty()) {
                    onLoginSuccess()
                }
            }) {
                Text("Anmelden")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ScannerTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
