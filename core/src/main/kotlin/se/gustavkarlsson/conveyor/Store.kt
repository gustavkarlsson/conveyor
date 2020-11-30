package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.internal.ActionExecutor
import se.gustavkarlsson.conveyor.internal.ActionIssuerImpl
import se.gustavkarlsson.conveyor.internal.StateTransformer
import se.gustavkarlsson.conveyor.internal.StoreImpl
import se.gustavkarlsson.conveyor.internal.Transformer
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
     * The job of this store, if it has been started.
     */
    public val job: Job?

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
    val actualInitialState = plugins.fold(initialState) { state, plugin ->
        plugin.overrideInitialState(state)
    }
    val actualStartActions = plugins.fold(startActions) { actions, plugin ->
        actions + plugin.addStartActions()
    }
    val actionTransformers = plugins.fold(emptyList<Transformer<Action<State>>>()) { transformers, plugin ->
        transformers + { plugin.transformActions(it) }
    }
    val stateTransformers = plugins.fold(emptyList<Transformer<State>>()) { transformers, plugin ->
        transformers + { plugin.transformStates(it) }
    }

    val actionIssuer = ActionIssuerImpl<State>()
    val state = UpdatableStateFlowImpl(actualInitialState)
    val actionExecutor = ActionExecutor(
        startActions = actualStartActions,
        actions = actionIssuer.issuedActions,
        transformers = actionTransformers,
        state = state,
    )
    val stateTransformer = StateTransformer(
        incomingState = state,
        transformers = stateTransformers,
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
