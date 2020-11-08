@file:Suppress("EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
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
    val state: StateFlow<State> = store.state

    override fun onEmailTextChanged(text: String) = store.issue(EmailChangeAction(text))

    override fun onPasswordTextChanged(text: String) = store.issue(PasswordChangeAction(text))

    override fun onLoginButtonClicked() = store.issue(LoginAction())

    override fun onLogoutButtonClicked() = store.issue { stateAccess ->
        stateAccess.set(State.Login())
    }
}

private class EmailChangeAction(private val text: String) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        stateAccess.update {
            require(this is State.Login)
            if (!isLoggingIn) {
                copy(emailText = text.trim().toLowerCase())
            } else this
        }
    }
}

private class PasswordChangeAction(private val text: String) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        stateAccess.update {
            require(this is State.Login)
            if (!isLoggingIn) {
                copy(passwordText = text)
            } else this
        }
    }
}

private class LoginAction : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        var progress = 0F
        while (progress < 1F) {
            if (stateAccess.state.value !is State.Login) return
            delay(Random.nextLong(100))
            progress += Random.nextFloat() / 10
            stateAccess.update {
                if (this is State.Login) {
                    copy(loginProgress = progress)
                } else this
            }
        }
        stateAccess.update {
            if (this is State.Login) {
                State.LoggedIn(emailText = emailText)
            } else this
        }
    }
}
