package se.gustavkarlsson.conveyor.rx2.test

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.StateAccess

class SimpleStateAccess<State>(initialState: State) : StateAccess<State> {
    private val mutableStateFlow = MutableStateFlow(initialState)

    override val state: StateFlow<State> = mutableStateFlow

    override suspend fun set(state: State) {
        mutableStateFlow.value = state
    }

    override suspend fun update(block: suspend (currentState: State) -> State): State {
        val newValue = block(mutableStateFlow.value)
        set(newValue)
        return newValue
    }
}
