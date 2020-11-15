package se.gustavkarlsson.conveyor.testing

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow

class NullAction<T> : Action<T> {
    override suspend fun execute(state: UpdatableStateFlow<T>) = Unit
}
