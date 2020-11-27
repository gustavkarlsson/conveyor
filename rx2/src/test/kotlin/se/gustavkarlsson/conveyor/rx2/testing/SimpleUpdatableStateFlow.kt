package se.gustavkarlsson.conveyor.rx2.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.UpdatableStateFlow

// TODO Near duplicate of UpdatableStateFlowImpl
class SimpleUpdatableStateFlow<State> private constructor(
    private val mutableFlow: MutableStateFlow<State>,
) : StateFlow<State> by mutableFlow, UpdatableStateFlow<State> {
    constructor(initialState: State) : this(MutableStateFlow(initialState))

    override suspend fun update(block: suspend State.() -> State): State {
        val newValue = mutableFlow.value.block()
        mutableFlow.value = newValue
        return newValue
    }

    override val subscriptionCount by mutableFlow::subscriptionCount
}
