package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import se.gustavkarlsson.conveyor.internal.CommandManager
import se.gustavkarlsson.conveyor.internal.LiveActionsManager
import se.gustavkarlsson.conveyor.internal.OpenActionsProcessor
import se.gustavkarlsson.conveyor.internal.StateManager
import se.gustavkarlsson.conveyor.internal.StoreImpl

@ExperimentalCoroutinesApi
@FlowPreview
public fun <State> buildStore(
    initialState: State,
    openActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
    commandBufferSize: Int = 64,
): Store<State> {
    val stateManager = StateManager(initialState)
    val commandManager = CommandManager(commandBufferSize, stateManager)
    val openActionsProcessor = OpenActionsProcessor(openActions)
    val liveActionsManager = LiveActionsManager(liveActions)
    val processors = listOf(commandManager, openActionsProcessor, liveActionsManager)
    val cancellables = listOf(liveActionsManager, commandManager, stateManager)
    return StoreImpl(stateManager, commandManager, liveActionsManager, processors, cancellables)
}
