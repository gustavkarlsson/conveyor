package se.gustavkarlsson.conveyor.rx2.test

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.StateAccess

class SimpleStateAccess<State>(initialState: State) : StateAccess<State> {
    private val mutableStateFlow = MutableStateFlow(initialState)

    override val state: StateFlow<State> = mutableStateFlow

    override suspend fun update(block: suspend State.() -> State): State {
        val newValue = mutableStateFlow.value.block()
        mutableStateFlow.value = newValue
        return newValue
    }
}
