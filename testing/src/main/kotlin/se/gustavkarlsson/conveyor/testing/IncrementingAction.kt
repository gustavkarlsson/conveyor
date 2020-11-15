package se.gustavkarlsson.conveyor.testing

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow

class IncrementingAction(private val increment: Int) : Action<Int> {
    override suspend fun execute(state: UpdatableStateFlow<Int>) {
        state.update { this + increment }
    }
}
