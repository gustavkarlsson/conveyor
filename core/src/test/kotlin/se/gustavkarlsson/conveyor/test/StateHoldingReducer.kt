package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Reducer

class StateHoldingReducer<T>(private var state: T) : Reducer<T> {
    override fun reduce(block: (T) -> T): T {
        val newState = block(state)
        state = newState
        return newState
    }
}
