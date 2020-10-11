package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.StateAccess

class StateHoldingStateAccess<T>(private var currentState: T) : StateAccess<T> {
    override fun update(block: (T) -> T): T {
        val newState = block(currentState)
        currentState = newState
        return newState
    }

    override fun get(): T = currentState

    override fun set(state: T) {
        currentState = state
    }
}
