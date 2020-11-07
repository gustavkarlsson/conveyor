@file:Suppress("FunctionName")

package se.gustavkarlsson.conveyor.demo

import androidx.compose.animation.animate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ProgressIndicatorConstants
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

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
        LoginIndicator(state.isLoginIndicatorVisible, state.loginIndicatorProgress)
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
private fun LoginIndicator(isVisible: Boolean, progress: Float) {
    val opacity = if (isVisible) 1F else 0F
    val animatedProgress = animate(
        target = progress,
        animSpec = ProgressIndicatorConstants.DefaultProgressAnimationSpec
    )
    LinearProgressIndicator(
        modifier = Modifier.padding(4.dp).drawOpacity(opacity),
        color = MaterialTheme.colors.secondary,
        progress = animatedProgress,
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
