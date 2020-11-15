package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.internal.ActionManagerImpl
import se.gustavkarlsson.conveyor.internal.StoreImpl
import se.gustavkarlsson.conveyor.internal.UpdatableStateFlowImpl

public interface Store<State> : ActionIssuer<State> {
    public val state: StateFlow<State>
    public fun start(scope: CoroutineScope): Job
}

@Suppress("FunctionName")
public fun <State> Store(initialState: State): Store<State> =
    StoreImpl(UpdatableStateFlowImpl(initialState), ActionManagerImpl())

public fun <State> CoroutineScope.start(store: Store<State>): Job =
    store.start(this)
