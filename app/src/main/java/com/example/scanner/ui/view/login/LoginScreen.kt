package com.example.scanner.ui.view.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scanner.R
import com.example.scanner.ui.viewmodel.LoginState
import com.example.scanner.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    isSyncing: Boolean = false,
    onLoginSuccess: (String) -> Unit
) {
    var personalNr by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            val employeeId = (loginState as LoginState.Success).employeeId
            onLoginSuccess(employeeId)
            viewModel.resetState()
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hemme_logo_full),
                    contentDescription = "Hemme Logo",
                    modifier = Modifier
                        .width(200.dp)
                        .padding(bottom = 32.dp)
                )

                Text("Bitte anmelden", style = MaterialTheme.typography.titleLarge)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = personalNr,
                    onValueChange = { newValue ->
                         if (newValue.all { it.isDigit() }) {
                             personalNr = newValue
                         }
                    },
                    label = { Text("Personal-Nr.") },
                    singleLine = true,
                    enabled = !isSyncing,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { viewModel.onLoginClicked(personalNr) },
                    enabled = loginState !is LoginState.Loading && !isSyncing
                ) {
                    Text("Anmelden")
                }
                
                if (isSyncing) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Text("Daten werden synchronisiert...", style = MaterialTheme.typography.bodySmall)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                when (loginState) {
                    is LoginState.Loading -> {
                        if (!isSyncing) CircularProgressIndicator()
                    }
                    is LoginState.Error -> {
                        Text(
                            text = (loginState as LoginState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {}
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Powered by",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.acontus_rgb),
                    contentDescription = "Acontus Logo",
                    modifier = Modifier.height(30.dp)
                )
            }
        }
    }
}
