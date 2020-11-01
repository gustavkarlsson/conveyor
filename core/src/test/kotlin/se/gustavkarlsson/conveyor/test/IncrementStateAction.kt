package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess

class IncrementStateAction(private val increment: Int = 1) : Action<Int> {
    override suspend fun execute(stateAccess: StateAccess<Int>) {
        stateAccess.update { it + increment }
    }
}
