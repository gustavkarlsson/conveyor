package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.UpdatableStateFlow

internal class UpdatableStateFlowImpl<State> private constructor(
    private val state: MutableStateFlow<State>,
) : StateFlow<State> by state, UpdatableStateFlow<State> {
    constructor(initialValue: State) : this(MutableStateFlow(initialValue))

    private val writeMutex = Mutex()
    override suspend fun update(block: suspend State.() -> State): State =
        writeMutex.withLock {
            val newState = value.block()
            state.value = newState
            newState
        }

    // FIXME verify internal/external subscription count
    override val subscriptionCount: StateFlow<Int> by state::subscriptionCount
}
