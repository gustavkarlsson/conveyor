@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import androidx.compose.animation.animate
import androidx.compose.desktop.Window
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

fun main() {
    val initialState = ViewState()
    runUi(ViewModel(initialState))
}

private fun runUi(viewModel: ViewModel) = Window(
    title = "Compose for Desktop",
    size = IntSize(400, 400),
) {
    val state = viewModel.state.collectAsState(viewModel.state.value)
    MaterialTheme {
        if (state.value.loginState != LoginState.LoggedIn) {
            LoginScreen(state, viewModel)
        } else {
            LoggedInScreen(state, viewModel)
        }
    }
}

@Composable
private fun LoginScreen(state: State<ViewState>, viewModel: ViewModel) {
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
fun LoginTitle() {
    Text(
        modifier = Modifier.padding(16.dp),
        text = "MySoft",
        style = MaterialTheme.typography.h3.copy(color = MaterialTheme.colors.primary),
    )
}

@Composable
fun EmailTextField(state: State<ViewState>, viewModel: ViewModel) {
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
fun PasswordTextField(state: State<ViewState>, viewModel: ViewModel) {
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
fun LoginIndicator(state: State<ViewState>) {
    val color = MaterialTheme.colors.secondary
    val backgroundColor = if (state.value.loginState is LoginState.LoggingIn) {
        color.copy(alpha = ProgressIndicatorConstants.DefaultIndicatorBackgroundOpacity)
    } else {
        Color.Transparent
    }
    val animatedProgress = animate(
        target = state.value.loginIndicatorProgress ?: 0f,
        animSpec = ProgressIndicatorConstants.DefaultProgressAnimationSpec
    )
    ProgressIndicatorConstants.DefaultProgressAnimationSpec
    LinearProgressIndicator(
        modifier = Modifier.padding(4.dp),
        color = color,
        backgroundColor = backgroundColor,
        progress = animatedProgress,
    )
}

@Composable
fun LoginButton(state: State<ViewState>, viewModel: ViewModel) {
    Button(
        modifier = Modifier.padding(8.dp),
        enabled = state.value.isLoginButtonEnabled,
        onClick = viewModel::onLoginButtonClicked,
    ) {
        Text("LOGIN")
    }
}


@Composable
fun LoggedInScreen(state: State<ViewState>, viewModel: ViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LoggedInTitle()
        LoggedInEmailText(state)
        LogoutButton(viewModel)
    }
}

@Composable
fun LoggedInTitle() {
    Text(
        modifier = Modifier.padding(8.dp),
        text = "Welcome",
        style = MaterialTheme.typography.h4.copy(color = MaterialTheme.colors.secondary),
    )
}

@Composable
fun LoggedInEmailText(state: State<ViewState>) {
    Text(
        modifier = Modifier.padding(8.dp),
        text = state.value.emailText,
        style = MaterialTheme.typography.h5,
    )
}

@Composable
fun LogoutButton(viewModel: ViewModel) {
    Button(
        modifier = Modifier.padding(8.dp),
        onClick = viewModel::onLogoutButtonClicked,
    ) {
        Text("LOGOUT")
    }
}
