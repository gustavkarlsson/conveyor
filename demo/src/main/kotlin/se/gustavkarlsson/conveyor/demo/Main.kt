@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.IntSize
import se.gustavkarlsson.conveyor.demo.screens.LoggedInScreen
import se.gustavkarlsson.conveyor.demo.screens.LoginScreen

fun main() {
    val initialState = State.Login()
    runUi(ViewModel(Api, initialState))
}

private fun runUi(viewModel: ViewModel) = Window(
    title = "Conveyor Demo",
    size = IntSize(400, 400),
) {
    val state = viewModel.state.collectAsState()
    MaterialTheme {
        when (val currentState = state.value) {
            is State.Login -> LoginScreen(currentState, viewModel)
            is State.LoggedIn -> LoggedInScreen(currentState, viewModel)
        }
    }
}
