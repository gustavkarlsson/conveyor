package se.gustavkarlsson.conveyor.demo.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.demo.Api
import se.gustavkarlsson.conveyor.demo.State

class LoginAction(private val api: Api) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        val newState = storeFlow.updateAndGet { state ->
            if (state is State.Login) {
                state.copy(isLoggingIn = true)
            } else state
        }
        if (newState !is State.Login) return
        val userName = api.login(newState.emailText, newState.passwordText)
        storeFlow.update { state ->
            when {
                state !is State.Login -> state
                userName == null -> state.copy(isLoggingIn = false, showInvalidLogin = true)
                else -> State.LoggedIn(name = userName)
            }
        }
    }
}
