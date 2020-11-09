package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.StateAccess

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateManager<State>(initialState: State) : StateAccess<State> {
    private val mutableFlow = MutableStateFlow(initialState)
    override val value by mutableFlow::value
    override val replayCache by mutableFlow::replayCache
    override val subscriptionCount = mutableFlow.subscriptionCount

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) = mutableFlow.collect(collector)

    private val writeMutex = Mutex()

    override suspend fun update(block: suspend State.() -> State): State =
        writeMutex.withLock {
            val state = value.block()
            mutableFlow.value = state
            state
        }
}
