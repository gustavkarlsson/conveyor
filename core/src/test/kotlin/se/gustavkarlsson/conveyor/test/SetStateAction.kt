package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdateState

class SetStateAction<State>(private val value: State) : Action<State> {
    override suspend fun execute(updateState: UpdateState<State>) {
        updateState { value }
    }
}
