package se.gustavkarlsson.conveyor

import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.gustavkarlsson.conveyor.internal.StateManager

public fun main() {
    val stateManager = StateManager(0, emptyList())

    var counter = 0

    runBlocking {
        launch { stateManager.run() }
        delay(100)
        stateManager.outgoingState
            .collect {
                println("Collected $it")
                stateManager.emit(++counter)
                stateManager.emit(++counter)
            }
    }
}
