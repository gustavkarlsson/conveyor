package se.gustavkarlsson.conveyor.testing

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.AtomicStateFlow

class NullAction<T> : Action<T> {
    override suspend fun execute(stateFlow: AtomicStateFlow<T>) = Unit
}
