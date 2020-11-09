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
internal class StateManager<State>(initialState: State) : AbstractFlow<State>(), UpdatableStateFlow<State> {
    private val mutableFlow = MutableStateFlow(initialState)
    override val value by mutableFlow::value
    override val replayCache by mutableFlow::replayCache
    override val subscriptionCount = mutableFlow.subscriptionCount

    override suspend fun collectSafely(collector: FlowCollector<State>) = mutableFlow.collect(collector::emit)

    private val writeMutex = Mutex()

    override suspend fun update(block: suspend State.() -> State): State =
        writeMutex.withLock {
            val state = value.block()
            mutableFlow.value = state
            state
        }
}
