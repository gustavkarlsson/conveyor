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
    val actionTransformers: Iterable<Transformer<Action<State>>> = emptyList()
    val stateTransformers: Iterable<Transformer<State>> = emptyList()

    val overriddenInitialState = plugins.override(initialState) { overrideInitialState(it) }
    val overriddenStartActions = plugins.override(startActions) { overrideStartActions(it) }
    val overriddenActionTransformers = plugins.override(actionTransformers) { overrideActionTransformers(it) }
    val overriddenStateTransformers = plugins.override(stateTransformers) { overrideStateTransformers(it) }

    val actionManager = ActionManagerImpl<State>()
    val updatableState = UpdatableStateFlowImpl(overriddenInitialState)
    val actionProcessor = ActionProcessor(
        startActions = overriddenStartActions,
        actionStream = actionManager.actions,
        transformers = overriddenActionTransformers,
        updatableState = updatableState
    )
    val stateProcessor = StateProcessor(updatableState, overriddenStateTransformers)
    return StoreImpl(
        stateFlow = stateProcessor.outgoingState,
        actionManager = actionManager,
        processors = listOf(stateProcessor, actionProcessor),
    )
}

public fun <State> CoroutineScope.start(store: Store<State>): Job =
    store.start(this)

private fun <State, T> Iterable<Plugin<State>>.override(
    value: T,
    operation: Plugin<State>.(T) -> T,
): T = fold(value) { acc: T, plugin: Plugin<State> ->
    plugin.operation(acc)
}
