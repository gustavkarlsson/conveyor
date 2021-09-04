package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import se.gustavkarlsson.conveyor.AtomicStateFlow

class SimpleAtomicStateFlow<State> private constructor(
    private val mutableFlow: MutableStateFlow<State>,
) : MutableSharedFlow<State> by mutableFlow, AtomicStateFlow<State> {
    constructor(initialState: State) : this(MutableStateFlow(initialState))

    override val value by mutableFlow::value
    override val storeSubscriberCount = MutableStateFlow(0)

    override suspend fun update(block: State.() -> State) = mutableFlow.update(block)

    override suspend fun updateAndGet(block: State.() -> State): State = mutableFlow.updateAndGet(block)

    override suspend fun getAndUpdate(block: State.() -> State): State = mutableFlow.getAndUpdate(block)

    @ExperimentalCoroutinesApi
    @Deprecated("Not supported", level = DeprecationLevel.HIDDEN)
    override fun resetReplayCache() {
        throw UnsupportedOperationException("${this::class.simpleName}.resetReplayCache is not supported")
    }
}
