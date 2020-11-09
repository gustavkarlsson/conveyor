package se.gustavkarlsson.conveyor.test

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.StateAccess

class SimpleStateAccess<State>(initialState: State) : StateAccess<State> {

    private val mutableFlow = MutableStateFlow(initialState)

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) = mutableFlow.collect(collector)
    override val subscriptionCount: StateFlow<Int> by mutableFlow::subscriptionCount
    override val replayCache: List<State> by mutableFlow::replayCache
    override val value: State by mutableFlow::value
    override val state: StateFlow<State> = mutableFlow

    override suspend fun update(block: suspend State.() -> State): State {
        val newState = mutableFlow.value.block()
        mutableFlow.value = newState
        return newState
    }
}
