package se.gustavkarlsson.conveyor.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import se.gustavkarlsson.conveyor.StateAccess

class StateHoldingStateAccess<T>(initialState: T) : StateAccess<T> {

    private val stateFlow = MutableStateFlow(initialState)

    override val flow: Flow<T> = stateFlow

    var currentState by stateFlow::value

    override fun get(): T = currentState

    override fun set(state: T) {
        currentState = state
    }

    override fun update(block: (T) -> T): T {
        val newState = block(currentState)
        currentState = newState
        return newState
    }
}
