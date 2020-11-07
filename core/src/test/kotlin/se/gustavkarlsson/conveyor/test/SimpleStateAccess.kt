package se.gustavkarlsson.conveyor.test

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.StateAccess

class SimpleStateAccess<T>(initialState: T) : StateAccess<T> {

    private val mutableFlow = MutableStateFlow(initialState)

    override val state: StateFlow<T> = mutableFlow

    var currentState by mutableFlow::value

    override suspend fun update(block: suspend (T) -> T): T {
        val newState = block(currentState)
        currentState = newState
        return newState
    }

    override suspend fun set(state: T) {
        currentState = state
    }
}
