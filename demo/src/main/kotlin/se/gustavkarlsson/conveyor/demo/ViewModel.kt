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

class ViewModel(initialState: State) : LoginEvents, LoggedInEvents {
    private val store = buildStore(initialState).apply { start(GlobalScope) }
    val state: Flow<State> = store.state
    val currentState: State get() = store.currentState

    override fun onEmailTextChanged(text: String) = store.issue(EmailChangeAction(text))

    override fun onPasswordTextChanged(text: String) = store.issue(PasswordChangeAction(text))

    override fun onLoginButtonClicked() = store.issue(LoginAction())

    override fun onLogoutButtonClicked() = store.issue { stateAccess ->
        stateAccess.set(State.Login())
    }
}

private class EmailChangeAction(private val text: String) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        stateAccess.update { state ->
            require(state is State.Login)
            if (state.loginStage == LoginStage.Initial) {
                state.copy(emailText = text.trim().toLowerCase())
            } else state
        }
    }
}

private class PasswordChangeAction(private val text: String) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        stateAccess.update { state ->
            require(state is State.Login)
            if (state.loginStage == LoginStage.Initial) {
                state.copy(passwordText = text)
            } else state
        }
    }
}

private class LoginAction : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        var progress = 0F
        while (progress < 1F) {
            if (stateAccess.get() !is State.Login) return
            delay(Random.nextLong(100))
            progress += Random.nextFloat() / 10
            stateAccess.update { state ->
                if (state is State.Login) {
                    state.copy(loginStage = LoginStage.LoggingIn(progress))
                } else state
            }
        }
        stateAccess.update { state ->
            if (state is State.Login) {
                State.LoggedIn(emailText = state.emailText)
            } else state
        }
    }
}
