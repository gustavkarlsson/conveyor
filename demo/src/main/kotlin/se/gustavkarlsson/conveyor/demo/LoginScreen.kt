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
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(state: State<ViewState.Login>, viewModel: ViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LoginTitle()
        EmailTextField(state, viewModel)
        PasswordTextField(state, viewModel)
        LoginIndicator(state)
        LoginButton(state, viewModel)
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
private fun EmailTextField(state: State<ViewState.Login>, viewModel: ViewModel) {
    OutlinedTextField(
        modifier = Modifier.padding(8.dp),
        value = state.value.emailText,
        onValueChange = viewModel::onEmailTextChanged,
        label = { Text("Email") },
        maxLines = 1,
        placeholder = { Text("someone@somewhere.com") },
    )
}

@Composable
private fun PasswordTextField(state: State<ViewState.Login>, viewModel: ViewModel) {
    OutlinedTextField(
        modifier = Modifier.padding(8.dp),
        value = state.value.passwordText,
        onValueChange = viewModel::onPasswordTextChanged,
        label = { Text("Password") },
        maxLines = 1,
        visualTransformation = PasswordVisualTransformation(),
    )
}

@Composable
private fun LoginIndicator(state: State<ViewState.Login>) {
    val opacity = if (state.value.isLoginIndicatorVisible) 1F else 0F
    val animatedProgress = animate(
        target = state.value.loginIndicatorProgress,
        animSpec = ProgressIndicatorConstants.DefaultProgressAnimationSpec
    )
    LinearProgressIndicator(
        modifier = Modifier.padding(4.dp).drawOpacity(opacity),
        color = MaterialTheme.colors.secondary,
        progress = animatedProgress,
    )
}

@Composable
private fun LoginButton(state: State<ViewState.Login>, viewModel: ViewModel) {
    Button(
        modifier = Modifier.padding(8.dp),
        enabled = state.value.isLoginButtonEnabled,
        onClick = viewModel::onLoginButtonClicked,
    ) {
        Text("LOGIN")
    }
}
