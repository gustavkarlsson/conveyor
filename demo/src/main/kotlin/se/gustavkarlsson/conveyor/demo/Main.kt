@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.IntSize

fun main() {
    val initialState = ViewState.Login()
    runUi(ViewModel(initialState))
}

private fun runUi(viewModel: ViewModel) = Window(
    title = "Compose for Desktop",
    size = IntSize(400, 400),
) {
    val state = viewModel.state.collectAsState(viewModel.currentState)
    MaterialTheme {
        when (val currentState = state.value) {
            is ViewState.Login -> LoginScreen(currentState, viewModel)
            is ViewState.LoggedIn -> LoggedInScreen(currentState, viewModel)
        }
    }
}
