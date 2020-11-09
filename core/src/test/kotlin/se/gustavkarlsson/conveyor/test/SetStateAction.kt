package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow

class SetStateAction<State>(private val value: State) : Action<State> {
    override suspend fun execute(stateAccess: UpdatableStateFlow<State>) {
        stateAccess.update { value }
    }
}
