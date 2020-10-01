package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Reducer

class NullAction<State> : Action<State> {
    override suspend fun execute(reducer: Reducer<State>) = Unit
}
