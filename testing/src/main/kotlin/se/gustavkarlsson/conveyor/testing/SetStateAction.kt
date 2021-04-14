package se.gustavkarlsson.conveyor.testing

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow

class SetStateAction<State>(private val value: State) : Action<State> {
    override suspend fun execute(stateFlow: UpdatableStateFlow<State>) {
        stateFlow.update { value }
    }
}
