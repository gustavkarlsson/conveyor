@file:Suppress("EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class ViewModel(initialState: ViewState) {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<ViewState> = mutableState

    fun onEmailTextChanged(text: String) = updateState {
        if (loginState == LoginState.Initial) {
            copy(emailText = text.trim().toLowerCase())
        } else this
    }

    fun onPasswordTextChanged(text: String) = updateState {
        if (loginState == LoginState.Initial) {
            copy(passwordText = text)
        } else this
    }

    fun onLoginButtonClicked() {
        var progress = 0F
        GlobalScope.launch {
            while (progress < 1F) {
                delay(Random.nextLong(500))
                progress += Random.nextFloat() / 10
                updateState {
                    copy(loginState = LoginState.LoggingIn(progress))
                }
            }
            updateState {
                copy(loginState = LoginState.LoggedIn)
            }
        }
    }

    fun onLogoutButtonClicked() = updateState {
        ViewState()
    }

    @Synchronized
    private fun updateState(block: ViewState.() -> ViewState) {
        mutableState.value = mutableState.value.block()
    }
}
