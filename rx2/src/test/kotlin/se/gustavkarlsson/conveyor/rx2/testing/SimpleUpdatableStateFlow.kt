package se.gustavkarlsson.conveyor.rx2.testing

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import se.gustavkarlsson.conveyor.UpdatableStateFlow

// TODO Near duplicate of StateManager
class SimpleUpdatableStateFlow<State>(initialState: State) : UpdatableStateFlow<State> {
    private val mutableFlow = MutableStateFlow(initialState)
    override val value by mutableFlow::value
    override val replayCache by mutableFlow::replayCache
    override val subscriptionCount by mutableFlow::subscriptionCount

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) = mutableFlow.collect(collector)

    override suspend fun update(block: suspend State.() -> State): State {
        val newValue = mutableFlow.value.block()
        mutableFlow.value = newValue
        return newValue
    }
}
