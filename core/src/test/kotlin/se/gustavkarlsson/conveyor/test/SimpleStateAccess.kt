package se.gustavkarlsson.conveyor.test

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.StateAccess

class SimpleStateAccess<State>(initialState: State) : StateAccess<State> {

    private val mutableFlow = MutableStateFlow(initialState)

    override val state: StateFlow<State> = mutableFlow

    var currentState by mutableFlow::value

    override suspend fun update(block: suspend State.() -> State): State {
        val newState = currentState.block()
        currentState = newState
        return newState
    }

    override suspend fun set(state: State) {
        currentState = state
    }
}
