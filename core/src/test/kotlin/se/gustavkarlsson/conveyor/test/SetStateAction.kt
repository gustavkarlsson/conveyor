package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow

class SetStateAction<State>(private val value: State) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) {
        state.update { value }
    }
}
