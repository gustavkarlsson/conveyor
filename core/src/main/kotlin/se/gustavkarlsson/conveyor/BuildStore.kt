package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import se.gustavkarlsson.conveyor.internal.LiveActionsManager
import se.gustavkarlsson.conveyor.internal.ManualActionsManager
import se.gustavkarlsson.conveyor.internal.OpenActionsProcessor
import se.gustavkarlsson.conveyor.internal.StateManager
import se.gustavkarlsson.conveyor.internal.StoreImpl

@ExperimentalCoroutinesApi
@FlowPreview
public fun <State> buildStore(
    initialState: State,
    openActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
    stateWatchers: Iterable<Watcher<State>> = emptyList(),
    plugins: Iterable<Plugin<State>> = emptyList(),
): Store<State> {
    val stateMappers = stateWatchers.map { it.toMapper() }.asIterable()

    val overriddenInitialState = initialState.override(plugins) { overrideInitialState(it) }
    val overriddenOpenActions = openActions.override(plugins) { overrideOpenActions(it) }
    val overriddenLiveActions = liveActions.override(plugins) { overrideLiveActions(it) }
    val overriddenStateMappers = stateMappers.override(plugins) { overrideStateMappers(it) }

    val stateManager = StateManager(overriddenInitialState, overriddenStateMappers)
    val openActionsProcessor = OpenActionsProcessor(overriddenOpenActions)
    val manualActionsManager = ManualActionsManager<State>()
    val liveActionsManager = LiveActionsManager(overriddenLiveActions)
    val actionProcessors = listOf(manualActionsManager, openActionsProcessor, liveActionsManager)
    val cancellables = listOf(liveActionsManager, manualActionsManager, stateManager)

    return StoreImpl(
        stateFlowProvider = stateManager,
        stateAccess = stateManager,
        actionIssuer = manualActionsManager,
        liveActionsCounter = liveActionsManager,
        actionProcessors = actionProcessors,
        cancellables = cancellables,
    )
}

private fun <T> Watcher<T>.toMapper(): Mapper<T> = WatchingMapper(this)

private class WatchingMapper<T>(private val watcher: Watcher<T>) : Mapper<T> {
    override fun map(value: T): T? {
        watcher.watch(value)
        return value
    }
}

private fun <State, T> T.override(
    plugins: Iterable<Plugin<State>>,
    operation: Plugin<State>.(T) -> T,
): T = plugins.fold(this) { acc: T, plugin: Plugin<State> ->
    plugin.operation(acc)
}
