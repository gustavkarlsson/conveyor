package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.internal.ActionManagerImpl
import se.gustavkarlsson.conveyor.internal.ActionProcessor
import se.gustavkarlsson.conveyor.internal.StateProcessor
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
    val actionTransformers = emptyList<Transformer<Action<State>>>().asIterable()
    val stateTransformers = emptyList<Transformer<State>>().asIterable()

    val overriddenInitialState = initialState.override(plugins) { overrideInitialState(it) }
    val overriddenStartActions = startActions.override(plugins) { overrideStartActions(it) }
    val overriddenActionTransformers = actionTransformers.override(plugins) { overrideActionTransformers(it) }
    val overriddenStateTransformers = stateTransformers.override(plugins) { overrideStateTransformers(it) }

    val actionManager = ActionManagerImpl<State>()
    val updatableState = UpdatableStateFlowImpl(overriddenInitialState)
    val stateProcessor = StateProcessor(updatableState, overriddenStateTransformers)
    val actionProcessor = ActionProcessor(
        startActions = overriddenStartActions,
        actionStream = actionManager.actions,
        transformers = overriddenActionTransformers,
        updatableState = updatableState
    )
    return StoreImpl(
        stateFlow = stateProcessor.outgoingState,
        actionManager = actionManager,
        processors = listOf(stateProcessor, actionProcessor),
    )
}

public fun <State> CoroutineScope.start(store: Store<State>): Job =
    store.start(this)

private fun <State, T> T.override(
    plugins: Iterable<Plugin<State>>,
    operation: Plugin<State>.(T) -> T,
): T = plugins.fold(this) { acc: T, plugin: Plugin<State> ->
    plugin.operation(acc)
}
