package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import se.gustavkarlsson.conveyor.UpdatableStateFlow

class SimpleUpdatableStateFlow<State>(initialState: State) : UpdatableStateFlow<State> {
    private val mutableFlow = MutableStateFlow(initialState)
    override val value by mutableFlow::value
    override val replayCache by mutableFlow::replayCache
    override val storeSubscriberCount = MutableStateFlow(0)

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) = mutableFlow.collect(collector)

    override suspend fun update(block: suspend State.() -> State): State {
        val newValue = mutableFlow.value.block()
        mutableFlow.value = newValue
        return newValue
    }
}
