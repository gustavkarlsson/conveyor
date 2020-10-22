package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import se.gustavkarlsson.conveyor.internal.*

@ExperimentalCoroutinesApi
@FlowPreview
public fun <State> buildStore(
    initialState: State,
    openActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
    stateWatchers: Iterable<Watcher<State>> = emptyList(),
    actionWatchers: Iterable<Watcher<Action<State>>> = emptyList(),
    plugins: Iterable<Plugin<State>> = emptyList(),
): Store<State> {
    val stateTransformers: Iterable<Transformer<State>> =
        stateWatchers.map { it.toMapper() }.asIterable()
    val actionTransformers: Iterable<Transformer<Action<State>>> =
        actionWatchers.map { it.toMapper() }.asIterable()

    val overriddenInitialState = initialState.override(plugins) { overrideInitialState(it) }
    val overriddenOpenActions = openActions.override(plugins) { overrideOpenActions(it) }
    val overriddenLiveActions = liveActions.override(plugins) { overrideLiveActions(it) }
    val overriddenStateTransformers = stateTransformers.override(plugins) { overrideStateTransformers(it) }
    val overriddenActionTransformers = actionTransformers.override(plugins) { overrideActionTransformers(it) }

    val stateManager = StateManager(overriddenInitialState, overriddenStateTransformers)
    val openActionsProcessor = OpenActionsProcessor(overriddenOpenActions)
    val manualActionsManager = ManualActionsManager<State>()
    val liveActionsManager = LiveActionsManager(overriddenLiveActions)
    val actionProcessors = listOf(manualActionsManager, openActionsProcessor, liveActionsManager)
    val cancellables = listOf(liveActionsManager, manualActionsManager, stateManager)

    return StoreImpl(
        stateAccess = stateManager,
        actionIssuer = manualActionsManager,
        liveActionsCounter = liveActionsManager,
        actionProcessors = actionProcessors,
        actionTransformers = overriddenActionTransformers,
        cancellables = cancellables,
    )
}

private fun <T> Watcher<T>.toMapper() = WatchingTransformer(this)

private class WatchingTransformer<T>(private val watcher: Watcher<T>) : Transformer<T> {
    override suspend fun transform(flow: Flow<T>): Flow<T> = flow.onEach { watcher.watch(it) }
}

private fun <State, T> T.override(
    plugins: Iterable<Plugin<State>>,
    operation: Plugin<State>.(T) -> T,
): T = plugins.fold(this) { acc: T, plugin: Plugin<State> ->
    plugin.operation(acc)
}
