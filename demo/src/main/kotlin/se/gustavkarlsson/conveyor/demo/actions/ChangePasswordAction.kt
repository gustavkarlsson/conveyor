package se.gustavkarlsson.conveyor.demo.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.AtomicStateFlow
import se.gustavkarlsson.conveyor.demo.State

class ChangePasswordAction(private val text: String) : Action<State> {
    override suspend fun execute(stateFlow: AtomicStateFlow<State>) {
        stateFlow.update {
            require(this is State.Login)
            if (!isLoggingIn) {
                copy(passwordText = text, showInvalidLogin = false)
            } else this
        }
    }
}
