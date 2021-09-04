package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.delay
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow

class IncrementingAction(
    private val increment: Int,
    private val delayMillis: Long = 0,
) : Action<Int> {
    override suspend fun execute(storeFlow: StoreFlow<Int>) {
        delay(delayMillis)
        storeFlow.update { it + increment }
    }
}
