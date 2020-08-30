package se.gustavkarlsson.cokrate

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

public interface Store<State : Any, Command : Any> {
    public fun start(scope: CoroutineScope = storeScope()): Job
    public val state: Flow<State>
    public suspend fun issue(command: Command)
}

private fun storeScope(): CoroutineScope =
    CoroutineScope(Dispatchers.Unconfined + CoroutineName("Store scope"))
