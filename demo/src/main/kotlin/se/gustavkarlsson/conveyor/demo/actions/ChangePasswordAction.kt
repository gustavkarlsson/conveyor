package se.gustavkarlsson.conveyor.demo.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.demo.State

class ChangePasswordAction(private val text: String) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        stateAccess.update {
            require(this is State.Login)
            if (!isLoggingIn) {
                copy(passwordText = text)
            } else this
        }
    }
}
