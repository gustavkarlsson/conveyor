@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
    val state = viewModel.state.collectAsState(viewModel.currentState)
    MaterialTheme {
        state.ifType<ViewState.Login> {
            LoginScreen(it, viewModel)
        }
        state.ifType<ViewState.LoggedIn> {
            LoggedInScreen(it, viewModel)
        }
    }
}

@Composable
private inline fun <reified T : ViewState> State<ViewState>.ifType(crossinline block: (State<T>) -> Unit) {
    val value = value
    if (value is T) {
        val loginState = remember(value) { mutableStateOf(value) }
        block(loginState)
    }
}
