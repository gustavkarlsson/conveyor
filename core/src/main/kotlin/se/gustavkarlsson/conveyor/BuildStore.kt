package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
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
): Store<State> = buildStore(
    initialState = initialState,
    openActions = openActions,
    liveActions = liveActions,
    stateWatchers = stateWatchers,
    actionWatchers = actionWatchers,
    plugins = plugins,
    scope = GlobalScope,
)

@ExperimentalCoroutinesApi
@FlowPreview
internal fun <State> buildStore(
    initialState: State,
    openActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
    stateWatchers: Iterable<Watcher<State>> = emptyList(),
    actionWatchers: Iterable<Watcher<Action<State>>> = emptyList(),
    plugins: Iterable<Plugin<State>> = emptyList(),
    scope: CoroutineScope,
): Store<State> {
    val stateSelectors: Iterable<Selector<State>> =
        stateWatchers.map { it.toSelector() }.asIterable()
    val actionTransformers: Iterable<Transformer<Action<State>>> =
        actionWatchers.map { it.toTransformer() }.asIterable()

    val overriddenInitialState = initialState.override(plugins) { overrideInitialState(it) }
    val overriddenOpenActions = openActions.override(plugins) { overrideOpenActions(it) }
    val overriddenLiveActions = liveActions.override(plugins) { overrideLiveActions(it) }
    val overriddenStateSelectors = stateSelectors.override(plugins) { overrideStateSelectors(it) }
    val overriddenActionTransformers = actionTransformers.override(plugins) { overrideActionTransformers(it) }

    val stateManager = StateManager(overriddenInitialState, overriddenStateSelectors, scope)
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

private fun <T> Watcher<T>.toTransformer() = WatchingTransformer(this)

private fun <T> Watcher<T>.toSelector() = WatchingSelector(this)

private class WatchingTransformer<T>(private val watcher: Watcher<T>) : Transformer<T> {
    override fun transform(flow: Flow<T>): Flow<T> = flow.onEach { watcher.watch(it) }
}

private class WatchingSelector<T>(private val watcher: Watcher<T>) : Selector<T> {
    override suspend fun select(old: T, new: T): T = new.also { watcher.watch(it) }
}

private fun <State, T> T.override(
    plugins: Iterable<Plugin<State>>,
    operation: Plugin<State>.(T) -> T,
): T = plugins.fold(this) { acc: T, plugin: Plugin<State> ->
    plugin.operation(acc)
}
