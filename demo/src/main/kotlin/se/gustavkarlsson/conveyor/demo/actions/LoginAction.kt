package se.gustavkarlsson.conveyor.demo.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.demo.Api
import se.gustavkarlsson.conveyor.demo.State

class LoginAction(private val api: Api) : Action<State> {
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
