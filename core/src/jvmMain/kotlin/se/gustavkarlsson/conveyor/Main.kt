package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

public fun main() {
    val stateManager = Store(
        initialState = 0,
        startActions = listOf(LockingAction()),
        lockHandler = LockHandler.Throw(1000),
    )

    runBlocking {
        stateManager.start(this)
        stateManager.state
            .collect {
                println("Collected $it")
            }
    }
}

private class LockingAction : Action<Int> {
    override suspend fun execute(stateFlow: AtomicStateFlow<Int>) {
        var counter = 0
        stateFlow.collect {
            stateFlow.emit(++counter)
            stateFlow.emit(++counter)
        }
    }
}
