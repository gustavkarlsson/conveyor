@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.demo.screens.LoggedInScreen
import se.gustavkarlsson.conveyor.demo.screens.LoginScreen

fun main() {
    val initialState = State.Login()
    val viewModel = ViewModel(Api, initialState)
    singleWindowApplication(
        title = "Conveyor Demo",
        state = WindowState(size = WindowSize(400.dp, 400.dp)),
    ) {
        val scope = rememberCoroutineScope()
        scope.launch { viewModel.run() }
        val state = viewModel.state.collectAsState()
        MaterialTheme {
            when (val currentState = state.value) {
                is State.Login -> LoginScreen(currentState, viewModel)
                is State.LoggedIn -> LoggedInScreen(currentState, viewModel)
            }
        }
    }
}
