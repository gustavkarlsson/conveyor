package se.gustavkarlsson.conveyor.demo.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.demo.State

class ChangeEmailAction(private val text: String) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) {
        state.update {
            require(this is State.Login)
            if (!isLoggingIn) {
                val sanitizedText = text.trim().toLowerCase()
                copy(emailText = sanitizedText)
            } else this
        }
    }
}
