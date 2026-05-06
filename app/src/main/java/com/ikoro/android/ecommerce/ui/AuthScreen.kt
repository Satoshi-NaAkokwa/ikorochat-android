package com.ikoro.android.ecommerce.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ikoro.android.ecommerce.viewmodel.AuthViewModel

/**
 * Authentication Screen
 * Handles user registration and login
 */
@Composable
fun AuthScreen(
    onAuthSuccess: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var isLogin by remember { mutableStateOf(false) }
    var publicKey by remember { mutableStateOf("") }
    var challenge by remember { mutableStateOf("") }
    var signature by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onAuthSuccess((authState as AuthState.Success).token)
            }
            is AuthState.Error -> {
                isLoading = false
                errorMessage = (authState as AuthState.Error).message
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("₿ Ọ F Ọ") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Title
            Text(
                text = if (isLogin) "Login" else "Register",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Public Key Input
            OutlinedTextField(
                value = publicKey,
                onValueChange = { publicKey = it },
                label = { Text("Public Key") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            if (isLogin) {
                // Challenge Input
                OutlinedTextField(
                    value = challenge,
                    onValueChange = { challenge = it },
                    label = { Text("Challenge") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                // Signature Input
                OutlinedTextField(
                    value = signature,
                    onValueChange = { signature = it },
                    label = { Text("Signature") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )
            }

            // Error Message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Register/Login Button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = ""
                    
                    if (isLogin) {
                        viewModel.login(
                            publicKey = publicKey,
                            challenge = challenge,
                            signature = signature
                        )
                    } else {
                        viewModel.register(
                            publicKey = publicKey,
                            deviceInfo = mapOf(
                                "platform" to "android",
                                "version" to "1.0.0"
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = publicKey.isNotEmpty() && !isLoading && 
                          (!isLogin || (challenge.isNotEmpty() && signature.isNotEmpty()))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isLogin) "Login" else "Register")
                }
            }

            // Toggle Login/Register
            TextButton(
                onClick = { 
                    isLogin = !isLogin
                    errorMessage = ""
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    if (isLogin) "Don't have an account? Register"
                    else "Already have an account? Login"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "₿ Ọ F Ọ - Decentralized E-Commerce",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}