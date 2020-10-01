package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Reducer

class FixedStateAction<State>(private val value: State) : Action<State> {
    override suspend fun execute(reducer: Reducer<State>) {
        reducer.reduce { value }
    }
}
