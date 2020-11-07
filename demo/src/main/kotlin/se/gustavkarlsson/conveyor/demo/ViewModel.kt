@file:Suppress("EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.buildStore
import kotlin.random.Random

interface LoginEvents {
    fun onEmailTextChanged(text: String)
    fun onPasswordTextChanged(text: String)
    fun onLoginButtonClicked()
}

interface LoggedInEvents {
    fun onLogoutButtonClicked()
}

class ViewModel(initialState: ViewState) : LoginEvents, LoggedInEvents {
    private val store = buildStore(initialState).apply { start(GlobalScope) }
    val state: Flow<ViewState> = store.state
    val currentState: ViewState get() = store.currentState

    override fun onEmailTextChanged(text: String) = store.issue(EmailChangeAction(text))

    override fun onPasswordTextChanged(text: String) = store.issue(PasswordChangeAction(text))

    override fun onLoginButtonClicked() = store.issue(LoginAction())

    override fun onLogoutButtonClicked() = store.issue { stateAccess ->
        stateAccess.set(ViewState.Login())
    }
}

private class EmailChangeAction(private val text: String) : Action<ViewState> {
    override suspend fun execute(stateAccess: StateAccess<ViewState>) {
        stateAccess.update { state ->
            require(state is ViewState.Login)
            if (state.loginState == LoginState.Initial) {
                state.copy(emailText = text.trim().toLowerCase())
            } else state
        }
    }
}

private class PasswordChangeAction(private val text: String) : Action<ViewState> {
    override suspend fun execute(stateAccess: StateAccess<ViewState>) {
        stateAccess.update { state ->
            require(state is ViewState.Login)
            if (state.loginState == LoginState.Initial) {
                state.copy(passwordText = text)
            } else state
        }
    }
}

private class LoginAction : Action<ViewState> {
    override suspend fun execute(stateAccess: StateAccess<ViewState>) {
        var progress = 0F
        while (progress < 1F) {
            if (stateAccess.get() !is ViewState.Login) return
            delay(Random.nextLong(100))
            progress += Random.nextFloat() / 10
            stateAccess.update { state ->
                if (state is ViewState.Login) {
                    state.copy(loginState = LoginState.LoggingIn(progress))
                } else state
            }
        }
        stateAccess.update { state ->
            if (state is ViewState.Login) {
                ViewState.LoggedIn(emailText = state.emailText)
            } else state
        }
    }
}
