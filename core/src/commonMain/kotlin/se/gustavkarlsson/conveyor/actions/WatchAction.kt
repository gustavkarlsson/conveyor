package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.flow.collect
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow

public abstract class WatchAction<State> : Action<State> {

    final override suspend fun execute(storeFlow: StoreFlow<State>) {
        storeFlow.collect { state ->
            onState(state)
        }
    }

    protected abstract fun onState(state: State)
}
