package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.internal.ActionExecutor
import se.gustavkarlsson.conveyor.internal.ActionIssuerImpl
import se.gustavkarlsson.conveyor.internal.StateTransformer
import se.gustavkarlsson.conveyor.internal.StoreImpl
import se.gustavkarlsson.conveyor.internal.UpdatableStateFlowImpl

/**
 * A predictable state container. The state can be read through [state]
 * and updated by issuing actions with [issue].
 *
 * The lifecycle of a store is simple. Initially the store is idle and will have it's initial state.
 * To make anything happen, the store must be started with [start].
 * This will run any "start actions" provided and also allows new actions to be issued.
 * The store is stopped when it's scope or job is cancelled. This cancels any running actions.
 */
public interface Store<State> : ActionIssuer<State> {
    /**
     * The state of the store. Always available regardless of which lifecycle stage the store is in.
     */
    public val state: StateFlow<State>

    /**
     * Starts the store in the provided scope.
     * This will run any "start actions" provided and also allows new actions to be issued.
     *
     * A store can only be started once.
     *
     * The store will be stopped when its scope or the returned [Job] is cancelled.
     */
    public fun start(scope: CoroutineScope): Job
}

/**
 * Creates a store with the given initial state.
 * Any start action provided will run automatically when the store is started.
 * Additionally, plugins can be provided to modify the Store's behavior even further.
 */
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

    val actionIssuer = ActionIssuerImpl<State>()
    val state = UpdatableStateFlowImpl(overriddenInitialState)
    val actionExecutor = ActionExecutor(
        startActions = overriddenStartActions,
        actions = actionIssuer.issuedActions,
        transformers = overriddenActionTransformers,
        state = state,
    )
    val stateTransformer = StateTransformer(
        incomingState = state,
        transformers = overriddenStateTransformers,
    )
    return StoreImpl(
        stateFlow = stateTransformer.outgoingState,
        actionIssuer = actionIssuer,
        launchers = listOf(stateTransformer, actionExecutor),
    )
}

/**
 * Starts the provided store in this scope.
 * This will run any "start actions" provided and also allows new actions to be issued.
 *
 * A store can only be started once.
 *
 * The store will be stopped when its scope or the returned [Job] is cancelled.
 */
public fun <State> CoroutineScope.start(store: Store<State>): Job =
    store.start(this)

private fun <State, T> Iterable<Plugin<State>>.override(
    value: T,
    operation: Plugin<State>.(T) -> T,
): T = fold(value) { acc: T, plugin: Plugin<State> ->
    plugin.operation(acc)
}
