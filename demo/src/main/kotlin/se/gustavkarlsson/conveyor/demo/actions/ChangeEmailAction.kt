package se.gustavkarlsson.conveyor.demo.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.demo.State

class ChangeEmailAction(private val text: String) : Action<State> {
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
