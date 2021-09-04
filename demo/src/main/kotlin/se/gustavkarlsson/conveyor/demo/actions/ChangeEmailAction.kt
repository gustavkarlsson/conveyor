package se.gustavkarlsson.conveyor.demo.actions

import java.util.Locale
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.demo.State

class ChangeEmailAction(private val text: String) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        storeFlow.update { state ->
            require(state is State.Login)
            if (!state.isLoggingIn) {
                val sanitizedText = text.trim().lowercase(Locale.getDefault())
                state.copy(emailText = sanitizedText, showInvalidLogin = false)
            } else state
        }
    }
}
