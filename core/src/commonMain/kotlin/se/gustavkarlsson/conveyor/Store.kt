package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.internal.ActionExecutor
import se.gustavkarlsson.conveyor.internal.ActionIssuerImpl
import se.gustavkarlsson.conveyor.internal.StateManager
import se.gustavkarlsson.conveyor.internal.StoreImpl
import se.gustavkarlsson.conveyor.internal.Transformer

/**
 * A predictable state container. The state can be read through [state]
 * and updated by issuing actions with [issue].
 *
 * The lifecycle of a store is simple. Initially the store is idle and will have it's initial state.
 * To make anything happen, the store must be started with [run].
 * This will run any "start actions" provided and also allows new actions to be issued.
 * The store is stopped when it's job completes. This cancels any running actions.
 */
public interface Store<State> : ActionIssuer<State> {
    /**
     * The state of the store. Always available regardless of which lifecycle stage the store is in.
     */
    public val state: StateFlow<State>

    /**
     * Runs the store.
     * This will run any "start actions" provided and also allows new actions to be issued.
     *
     * A store can only be run once and will run until cancelled.
     */
    public suspend fun run(): Nothing
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
    val stateManager = StateManager(actualInitialState, stateTransformers)
    val actionExecutor = ActionExecutor(
        startActions = actualStartActions,
        actions = actionIssuer.issuedActions,
        transformers = actionTransformers,
        storeFlow = stateManager,
    )
    return StoreImpl(
        stateFlow = stateManager.outgoingState,
        actionIssuer = actionIssuer,
        processes = listOf(stateManager, actionExecutor),
    )
}
