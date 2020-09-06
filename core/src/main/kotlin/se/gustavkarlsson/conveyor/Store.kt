package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

public interface Store<State> {
    public val state: Flow<State>
    public val currentState: State
    public fun start(scope: CoroutineScope): Job
    public suspend fun issue(command: Command<State>)
}

public object StoreStoppedException : IllegalStateException("Store has been stopped")

public object StoreStartedException : IllegalStateException("Store has already been started")
