package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow

class IncrementStateAction(private val increment: Int = 1) : Action<Int> {
    override suspend fun execute(state: UpdatableStateFlow<Int>) {
        state.update { this + increment }
    }
}
