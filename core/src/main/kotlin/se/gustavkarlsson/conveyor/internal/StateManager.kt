package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.StateAccess

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateManager<State>(initialState: State) : StateAccess<State> {
    private val mutableFlow = MutableStateFlow(initialState)

    override val state: StateFlow<State> = mutableFlow

    val subscriptionCount: Flow<Int> = mutableFlow.subscriptionCount

    private val writeMutex = Mutex()

    override suspend fun set(state: State) {
        writeMutex.withLock {
            mutableFlow.value = state
        }
    }

    override suspend fun update(block: suspend State.() -> State): State =
        writeMutex.withLock {
            val state = state.value.block()
            mutableFlow.value = state
            state
        }
}
