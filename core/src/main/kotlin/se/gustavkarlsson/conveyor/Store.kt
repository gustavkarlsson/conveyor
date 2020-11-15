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
public fun <State> Store(
    initialState: State,
    startActions: Iterable<Action<State>> = emptyList(),
    plugins: Iterable<Plugin<State>> = emptyList(),
): Store<State> {
    val emptyList = emptyList<Transformer<Action<State>>>().asIterable()

    val overriddenInitialState = initialState.override(plugins) { overrideInitialState(it) }
    val overriddenStartActions = startActions.override(plugins) { overrideStartActions(it) }
    val overriddenActionTransformers = emptyList.override(plugins) { overrideActionTransformers(it) }

    val updatableStateFlow = UpdatableStateFlowImpl(overriddenInitialState)
    val actionManager = ActionManagerImpl<State>()
    return StoreImpl(updatableStateFlow, actionManager, overriddenStartActions, overriddenActionTransformers)
}

public fun <State> CoroutineScope.start(store: Store<State>): Job =
    store.start(this)

private fun <State, T> T.override(
    plugins: Iterable<Plugin<State>>,
    operation: Plugin<State>.(T) -> T,
): T = plugins.fold(this) { acc: T, plugin: Plugin<State> ->
    plugin.operation(acc)
}
