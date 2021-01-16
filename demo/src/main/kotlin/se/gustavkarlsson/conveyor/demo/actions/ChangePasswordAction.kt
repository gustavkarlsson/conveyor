package se.gustavkarlsson.conveyor.demo.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.demo.State

class ChangePasswordAction(private val text: String) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) {
        state.update {
            require(this is State.Login)
            if (!isLoggingIn) {
                copy(passwordText = text, showInvalidLogin = false)
            } else this
        }
    }
}
