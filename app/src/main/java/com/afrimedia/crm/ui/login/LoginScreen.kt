package com.afrimedia.crm.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.afrimedia.crm.data.remote.ApiClient
import com.afrimedia.crm.data.remote.SessionManager
import com.afrimedia.crm.ui.theme.AmiBlack
import com.afrimedia.crm.ui.theme.AmiOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(session: SessionManager, onLoggedIn: () -> Unit) {
    var siteUrl by remember { mutableStateOf(session.siteUrl.ifBlank { "https://" }) }
    var username by remember { mutableStateOf(session.username) }
    var appPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Center
        ) {
            LogoMark()
            Text("Afri Media CRM", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(
                "Sign in with your WordPress account and an Application Password.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = siteUrl,
                onValueChange = { siteUrl = it },
                label = { Text("Site address") },
                placeholder = { Text("https://yoursite.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("WordPress username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = appPassword,
                onValueChange = { appPassword = it },
                label = { Text("Application Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            )
            Text(
                "Create one under your WordPress profile: Users → your profile → Application Passwords. This is not your normal login password.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 12.dp))
            }

            Button(
                onClick = {
                    val cleanUrl = siteUrl.trim().trimEnd('/')
                    if (cleanUrl.isBlank() || username.isBlank() || appPassword.isBlank()) {
                        errorMessage = "Please fill in all three fields."
                        return@Button
                    }
                    errorMessage = null
                    isLoading = true
                    scope.launch {
                        try {
                            val api = ApiClient.probe(cleanUrl, username.trim(), appPassword.trim())
                            val response = api.getMe()
                            if (response.isSuccessful) {
                                session.saveCredentials(cleanUrl, username.trim(), appPassword.trim())
                                onLoggedIn()
                            } else if (response.code() == 401 || response.code() == 403) {
                                errorMessage = "Login failed — check your username and Application Password."
                            } else {
                                errorMessage = "Couldn't verify the connection (HTTP ${response.code()}). Make sure the CRM plugin is up to date and the site uses HTTPS."
                            }
                        } catch (e: Exception) {
                            errorMessage = "Couldn't reach that site. Check the address and your connection."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text(if (isLoading) "Connecting…" else "Sign in")
            }
        }
    }
}

@Composable
private fun LogoMark() {
    Text(
        buildAnnotatedLogo(),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Black
    )
}

private fun buildAnnotatedLogo() = androidx.compose.ui.text.buildAnnotatedString {
    withStyle(androidx.compose.ui.text.SpanStyle(color = AmiBlack)) { append("AFRI") }
    withStyle(androidx.compose.ui.text.SpanStyle(color = AmiOrange)) { append("MEDIA") }
}
