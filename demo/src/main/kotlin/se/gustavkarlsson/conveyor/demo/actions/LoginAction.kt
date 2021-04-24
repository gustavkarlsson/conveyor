package se.gustavkarlsson.conveyor.demo.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.AtomicStateFlow
import se.gustavkarlsson.conveyor.demo.Api
import se.gustavkarlsson.conveyor.demo.State

class LoginAction(private val api: Api) : Action<State> {
    override suspend fun execute(stateFlow: AtomicStateFlow<State>) {
        val newState = stateFlow.update {
            if (this is State.Login) {
                copy(isLoggingIn = true)
            } else this
        }
        if (newState !is State.Login) return
        val userName = api.login(newState.emailText, newState.passwordText)
        stateFlow.update {
            when {
                this !is State.Login -> this
                userName == null -> copy(isLoggingIn = false, showInvalidLogin = true)
                else -> State.LoggedIn(name = userName)
            }
        }
    }
}
