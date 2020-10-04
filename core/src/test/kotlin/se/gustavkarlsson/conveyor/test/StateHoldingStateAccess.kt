package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.StateAccess

class StateHoldingStateAccess<T>(override var currentState: T) : StateAccess<T> {
    override fun update(block: (T) -> T): T {
        val newState = block(currentState)
        currentState = newState
        return newState
    }
}
