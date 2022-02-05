package se.gustavkarlsson.conveyor.testing

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow

class SetStateAction<State>(private val value: State) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        storeFlow.emit(value)
    }
}
