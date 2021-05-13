package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.flow.collect
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.AtomicStateFlow

public abstract class WatchAction<State> : Action<State> {

    final override suspend fun execute(stateFlow: AtomicStateFlow<State>) {
        stateFlow.collect { state ->
            onState(state)
        }
    }

    protected abstract fun onState(state: State)
}
