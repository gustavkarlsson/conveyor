@file:Suppress("FunctionName")

package se.gustavkarlsson.conveyor.demo.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import se.gustavkarlsson.conveyor.demo.LoginEvents
import se.gustavkarlsson.conveyor.demo.State

@Composable
fun LoginScreen(state: State.Login, events: LoginEvents) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LoginTitle()
        EmailTextField(state.emailText, events::onEmailTextChanged)
        PasswordTextField(state.passwordText, events::onPasswordTextChanged)
        LoginIndicator(state.isLoginIndicatorVisible)
        LoginButton(state.isLoginButtonEnabled, events::onLoginButtonClicked)
    }
}

@Composable
private fun LoginTitle() {
    Text(
        modifier = Modifier.padding(16.dp),
        text = "MySoft",
        style = MaterialTheme.typography.h3.copy(color = MaterialTheme.colors.primary),
    )
}

@Composable
private fun EmailTextField(text: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.padding(8.dp),
        value = text,
        onValueChange = onChange,
        label = { Text("Email") },
        maxLines = 1,
        placeholder = { Text("someone@somewhere.com") },
    )
}

@Composable
private fun PasswordTextField(text: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.padding(8.dp),
        value = text,
        onValueChange = onChange,
        label = { Text("Password") },
        maxLines = 1,
        visualTransformation = PasswordVisualTransformation(),
    )
}

@Composable
private fun LoginIndicator(isVisible: Boolean) {
    val opacity = if (isVisible) 1F else 0F
    LinearProgressIndicator(
        modifier = Modifier.padding(4.dp).alpha(opacity),
        color = MaterialTheme.colors.secondary,
    )
}

@Composable
private fun LoginButton(isEnabled: Boolean, onClick: () -> Unit) {
    Button(
        modifier = Modifier.padding(8.dp),
        enabled = isEnabled,
        onClick = onClick,
    ) {
        Text("LOGIN")
    }
}
