package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import se.gustavkarlsson.conveyor.internal.LiveActionsManager
import se.gustavkarlsson.conveyor.internal.ManualActionsManager
import se.gustavkarlsson.conveyor.internal.StartActionProcessor
import se.gustavkarlsson.conveyor.internal.StateManager
import se.gustavkarlsson.conveyor.internal.StoreImpl

@ExperimentalCoroutinesApi
@FlowPreview
public fun <State> buildStore(
    initialState: State,
    startActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
    plugins: Iterable<Plugin<State>> = emptyList(),
): Store<State> {
    val overriddenInitialState = initialState.override(plugins) { overrideInitialState(it) }
    val overriddenStartActions = startActions.override(plugins) { overrideStartActions(it) }
    val overriddenLiveActions = liveActions.override(plugins) { overrideLiveActions(it) }

    val stateManager = StateManager(overriddenInitialState)
    val startActionProcessor = StartActionProcessor(overriddenStartActions)
    val manualActionsManager = ManualActionsManager<State>()
    val liveActionsManager = LiveActionsManager(overriddenLiveActions)
    val actionProcessors = listOf(manualActionsManager, startActionProcessor, liveActionsManager)
    val cancellables = listOf(liveActionsManager, manualActionsManager, stateManager)

    return StoreImpl(
        stateAccess = stateManager,
        actionIssuer = manualActionsManager,
        liveActionsCounter = liveActionsManager,
        actionProcessors = actionProcessors,
        cancellables = cancellables,
    )
}

private fun <State, T> T.override(
    plugins: Iterable<Plugin<State>>,
    operation: Plugin<State>.(T) -> T,
): T = plugins.fold(this) { acc: T, plugin: Plugin<State> ->
    plugin.operation(acc)
}
