package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.StateAccess

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateManager<State>(initialState: State) : StateAccess<State> {
    private val mutableFlow = MutableStateFlow(initialState)

    @Deprecated("Marked for deletion", level = DeprecationLevel.WARNING)
    override val state: StateFlow<State> = mutableFlow

    override val subscriptionCount: StateFlow<Int> = mutableFlow.subscriptionCount

    private val writeMutex = Mutex()

    override suspend fun update(block: suspend State.() -> State): State =
        writeMutex.withLock {
            val state = state.value.block()
            mutableFlow.value = state
            state
        }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) = mutableFlow.collect(collector)

    override val replayCache: List<State> by mutableFlow::replayCache

    override val value: State by mutableFlow::value
}
