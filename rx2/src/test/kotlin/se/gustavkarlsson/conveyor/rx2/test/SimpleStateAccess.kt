package se.gustavkarlsson.conveyor.rx2.test

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.StateAccess

// TODO Duplicate of class in core
class SimpleStateAccess<State>(initialState: State) : StateAccess<State> {
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
