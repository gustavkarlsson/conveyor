package se.gustavkarlsson.conveyor.rx2.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import se.gustavkarlsson.conveyor.StateAccess

class SimpleStateAccess<State>(initialState: State) : StateAccess<State> {
    private val mutableStateFlow = MutableStateFlow(initialState)

    override val flow: Flow<State> = mutableStateFlow

    override fun get(): State = mutableStateFlow.value

    override suspend fun set(state: State) {
        mutableStateFlow.value = state
    }

    override suspend fun update(block: suspend (currentState: State) -> State): State {
        val newValue = block(mutableStateFlow.value)
        set(newValue)
        return newValue
    }
}
