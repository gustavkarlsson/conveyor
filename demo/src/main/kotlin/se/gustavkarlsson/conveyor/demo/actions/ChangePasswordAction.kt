package se.gustavkarlsson.conveyor.demo.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.demo.State

class ChangePasswordAction(private val text: String) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        storeFlow.update { state ->
            require(state is State.Login)
            if (!state.isLoggingIn) {
                state.copy(passwordText = text, showInvalidLogin = false)
            } else state
        }
    }
}
