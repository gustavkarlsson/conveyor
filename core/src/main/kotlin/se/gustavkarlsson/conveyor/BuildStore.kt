package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import se.gustavkarlsson.conveyor.store.CommandManager
import se.gustavkarlsson.conveyor.store.LiveActionsProcessorImpl
import se.gustavkarlsson.conveyor.store.StartActionsProcessorImpl
import se.gustavkarlsson.conveyor.store.StateHolderImpl
import se.gustavkarlsson.conveyor.store.StoreImpl

@ExperimentalCoroutinesApi
@FlowPreview
public fun <State> buildStore(
    initialState: State,
    startActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
    commandBufferSize: Int = 64,
): Store<State> {
    val stateHolder = StateHolderImpl(initialState)
    val commandManager = CommandManager(commandBufferSize, stateHolder::get, stateHolder::set)
    val startActionsProcessor = StartActionsProcessorImpl(startActions, commandManager)
    val liveActionsProcessor = LiveActionsProcessorImpl(liveActions, commandManager)
    return StoreImpl(stateHolder, commandManager, commandManager, startActionsProcessor, liveActionsProcessor)
}
