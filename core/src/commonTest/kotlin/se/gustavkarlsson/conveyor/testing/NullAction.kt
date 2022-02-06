package se.gustavkarlsson.conveyor.testing

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow

class NullAction<T> : Action<T> {
    override suspend fun execute(storeFlow: StoreFlow<T>) = Unit
}
