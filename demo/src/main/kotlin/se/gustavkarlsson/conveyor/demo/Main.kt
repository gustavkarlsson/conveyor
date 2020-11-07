@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntSize

fun main() {
    val initialState = ViewState.Login()
    runUi(ViewModel(initialState))
}

private fun runUi(viewModel: ViewModel) = Window(
    title = "Compose for Desktop",
    size = IntSize(400, 400),
) {
    val state = viewModel.state.collectAsState(viewModel.state.value)
    MaterialTheme {
        val currentState = state.value
        if (currentState is ViewState.Login) {
            val loginState = remember(currentState) { mutableStateOf(currentState) }
            LoginScreen(loginState, viewModel)
        }
        if (currentState is ViewState.LoggedIn) {
            val loggedInState = remember(currentState) { mutableStateOf(currentState) }
            LoggedInScreen(loggedInState, viewModel)
        }
    }
}
