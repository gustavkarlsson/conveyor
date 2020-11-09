package se.gustavkarlsson.conveyor.demo.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.demo.Api
import se.gustavkarlsson.conveyor.demo.State

class LoginAction(private val api: Api) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) {
        val newState = state.update {
            if (this is State.Login) {
                copy(isLoggingIn = true)
            } else this
        }
        if (newState !is State.Login) return
        val userName = api.login(newState.emailText, newState.passwordText)
        state.update {
            when {
                this !is State.Login -> this
                userName == null -> copy(isLoggingIn = false)
                else -> State.LoggedIn(name = userName)
            }
        }
    }
}
