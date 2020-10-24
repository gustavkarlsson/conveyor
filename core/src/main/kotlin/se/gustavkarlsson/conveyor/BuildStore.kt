package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import se.gustavkarlsson.conveyor.internal.LiveActionsManager
import se.gustavkarlsson.conveyor.internal.ManualActionsManager
import se.gustavkarlsson.conveyor.internal.StartActionFlowProvider
import se.gustavkarlsson.conveyor.internal.StateManager
import se.gustavkarlsson.conveyor.internal.StoreImpl

@ExperimentalCoroutinesApi
@FlowPreview
public fun <State> buildStore(
    initialState: State,
    startActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
    stateWatchers: Iterable<Watcher<State>> = emptyList(),
    actionWatchers: Iterable<Watcher<Action<State>>> = emptyList(),
    plugins: Iterable<Plugin<State>> = emptyList(),
): Store<State> {
    val actionTransformers: Iterable<Transformer<Action<State>>> =
        actionWatchers.map { WatchingTransformer(it) }
    val watcherActions: Iterable<Action<State>> =
        stateWatchers.map { WatchingAction(it) }

    val overriddenInitialState = initialState.override(plugins) { overrideInitialState(it) }
    val overriddenStartActions = (startActions + watcherActions).asIterable().override(plugins) { overrideStartActions(it) }
    val overriddenLiveActions = liveActions.override(plugins) { overrideLiveActions(it) }
    val overriddenActionTransformers = actionTransformers.override(plugins) { overrideActionTransformers(it) }

    val stateManager = StateManager(overriddenInitialState)
    val startActionFlowProvider = StartActionFlowProvider(overriddenStartActions)
    val manualActionsManager = ManualActionsManager<State>()
    val liveActionsManager = LiveActionsManager(overriddenLiveActions)
    val actionFlow = listOf(manualActionsManager, startActionFlowProvider, liveActionsManager)
        .map { it.actionFlow }
        .merge()
        .compose(overriddenActionTransformers)
    val cancellables = listOf(liveActionsManager, manualActionsManager, stateManager)

    return StoreImpl(
        stateAccess = stateManager,
        actionIssuer = manualActionsManager,
        liveActionsCounter = liveActionsManager,
        actionFlow = actionFlow,
        cancellables = cancellables,
    )
}

private class WatchingTransformer<T>(private val watcher: Watcher<T>) : Transformer<T> {
    override fun transform(flow: Flow<T>): Flow<T> = flow.onEach { watcher.watch(it) }
}

private class WatchingAction<T>(private val watcher: Watcher<T>) : Action<T> {
    override suspend fun execute(stateAccess: StateAccess<T>) {
        stateAccess.flow.collect { state ->
            watcher.watch(state)
        }
    }
}

private fun <State, T> T.override(
    plugins: Iterable<Plugin<State>>,
    operation: Plugin<State>.(T) -> T,
): T = plugins.fold(this) { acc: T, plugin: Plugin<State> ->
    plugin.operation(acc)
}

private fun <T> Flow<T>.compose(transformers: Iterable<Transformer<T>>): Flow<T> =
    transformers.fold(this) { flow, transformer ->
        transformer.transform(flow)
    }
