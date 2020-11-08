@file:Suppress("EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.buildStore
import se.gustavkarlsson.conveyor.issue

interface LoginEvents {
    fun onEmailTextChanged(text: String)
    fun onPasswordTextChanged(text: String)
    fun onLoginButtonClicked()
}

interface LoggedInEvents {
    fun onOperationButtonClicked()
    fun onLogoutButtonClicked()
}

class ViewModel(api: Api, initialState: State) : LoginEvents, LoggedInEvents {
    private val store = buildStore(initialState).apply { start(GlobalScope) }
    val state: StateFlow<State> = store.state
    private val loginAction = LoginAction(api)
    private val operationAction = OperationAction(api)

    override fun onEmailTextChanged(text: String) = store.issue(ChangeEmailAction(text))

    override fun onPasswordTextChanged(text: String) = store.issue(ChangePasswordAction(text))

    override fun onLoginButtonClicked() = store.issue(loginAction)

    override fun onOperationButtonClicked() = store.issue(operationAction)

    override fun onLogoutButtonClicked() = store.issue { stateAccess ->
        stateAccess.set(State.Login())
    }
}

private class ChangeEmailAction(private val text: String) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        stateAccess.update {
            require(this is State.Login)
            if (!isLoggingIn) {
                val sanitizedText = text.trim().toLowerCase()
                copy(emailText = sanitizedText)
            } else this
        }
    }
}

private class ChangePasswordAction(private val text: String) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        stateAccess.update {
            require(this is State.Login)
            if (!isLoggingIn) {
                copy(passwordText = text)
            } else this
        }
    }
}

private class LoginAction(private val api: Api) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        val state = stateAccess.update {
            if (this is State.Login) {
                copy(isLoggingIn = true)
            } else this
        }
        if (state !is State.Login) return
        val userName = api.login(state.emailText, state.passwordText)
        stateAccess.update {
            when {
                this !is State.Login -> this
                userName == null -> copy(isLoggingIn = false)
                else -> State.LoggedIn(name = userName)
            }
        }
    }
}

private class OperationAction(private val api: Api) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        api.operation().collect { progress ->
            stateAccess.update {
                if (this is State.LoggedIn) {
                    copy(operationProgress = progress)
                } else this
            }
        }
        stateAccess.update {
            if (this is State.LoggedIn) {
                copy(operationProgress = null)
            } else this
        }
    }
}
