package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.UpdatableStateFlow

@OptIn(FlowPreview::class)
internal class UpdatableStateFlowImpl<State>(initialState: State) : AbstractFlow<State>(), UpdatableStateFlow<State> {
    private val state = MutableStateFlow(initialState)
    override val value by state::value
    override val replayCache by state::replayCache
    override val subscriptionCount by state::subscriptionCount

    private val writeMutex = Mutex()
    override suspend fun update(block: suspend State.() -> State): State =
        writeMutex.withLock {
            val newState = value.block()
            state.value = newState
            newState
        }

    override suspend fun collectSafely(collector: FlowCollector<State>) = state.collect(collector::emit)
}
