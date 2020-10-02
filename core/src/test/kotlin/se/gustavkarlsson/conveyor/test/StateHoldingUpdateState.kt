package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.UpdateState

class StateHoldingUpdateState<T>(private var state: T) : UpdateState<T> {
    override fun invoke(block: (T) -> T): T {
        val newState = block(state)
        state = newState
        return newState
    }
}
