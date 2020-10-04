package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess

class NullAction<State> : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) = Unit
}
