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
        require(this is ViewState.Login)
        if (loginState == LoginState.Initial) {
            copy(emailText = text.trim().toLowerCase())
        } else this
    }

    fun onPasswordTextChanged(text: String) = updateState {
        require(this is ViewState.Login)
        if (loginState == LoginState.Initial) {
            copy(passwordText = text)
        } else this
    }

    fun onLoginButtonClicked() {
        var progress = 0F
        GlobalScope.launch {
            while (progress < 1F) {
                if (mutableState.value !is ViewState.Login) return@launch
                delay(Random.nextLong(100))
                progress += Random.nextFloat() / 10
                updateState {
                    if (this is ViewState.Login) {
                        copy(loginState = LoginState.LoggingIn(progress))
                    } else this
                }
            }
            updateState {
                if (this is ViewState.Login) {
                    ViewState.LoggedIn(emailText = emailText)
                } else this
            }
        }
    }

    fun onLogoutButtonClicked() = updateState {
        ViewState.Login()
    }

    @Synchronized
    private fun updateState(block: ViewState.() -> ViewState) {
        mutableState.value = mutableState.value.block()
    }
}
