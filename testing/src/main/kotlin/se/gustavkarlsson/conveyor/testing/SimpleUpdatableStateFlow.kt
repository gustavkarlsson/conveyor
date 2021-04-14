package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import se.gustavkarlsson.conveyor.UpdatableStateFlow

class SimpleUpdatableStateFlow<State> private constructor(
    private val mutableFlow: MutableStateFlow<State>,
) : MutableSharedFlow<State> by mutableFlow, UpdatableStateFlow<State> {
    constructor(initialState: State) : this(MutableStateFlow(initialState))

    override val value by mutableFlow::value
    override val storeSubscriberCount = MutableStateFlow(0)

    override suspend fun update(block: suspend State.() -> State): State {
        val newValue = mutableFlow.value.block()
        mutableFlow.value = newValue
        return newValue
    }

    @ExperimentalCoroutinesApi
    @Deprecated("Not supported", level = DeprecationLevel.HIDDEN)
    override fun resetReplayCache() {
        throw UnsupportedOperationException("UpdatableStateFlow.resetReplayCache is not supported")
    }
}
