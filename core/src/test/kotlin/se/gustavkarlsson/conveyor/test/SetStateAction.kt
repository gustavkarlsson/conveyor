package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess

class SetStateAction<State>(private val value: State) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        stateAccess.set(value)
    }
}
