package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.StoreStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException
import java.util.concurrent.atomic.AtomicReference

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    initialState: State,
    startActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
    commandBufferSize: Int = DEFAULT_BUFFER_SIZE,
) : Store<State> {

    private val stateHolder = StateHolderImpl(initialState)

    private val commandProcessor = CommandManager(commandBufferSize, stateHolder::get, stateHolder::set)

    private val startActionsProcessor = StartActionsProcessorImpl(startActions, commandProcessor)

    private val liveActionsProcessor = LiveActionsProcessorImpl(liveActions, commandProcessor)

    override val state = stateHolder.flow
        .onStart { liveActionsProcessor.increaseLiveCount() }
        .onCompletion { liveActionsProcessor.decreaseLiveCount() }

    override val currentState get() = stateHolder.get()

    private val stage = AtomicReference(Stage.Initial)

    override fun start(scope: CoroutineScope): Job {
        if (!stage.compareAndSet(Stage.Initial, Stage.Started)) {
            throw StoreStartedException
        }
        val job = scope.launch {
            launch { commandProcessor.process(scope) }
            launch { startActionsProcessor.process(scope) }
            launch { liveActionsProcessor.process(scope) }
        }
        job.invokeOnCompletion { throwable ->
            stage.set(Stage.Stopped)
            liveActionsProcessor.close(throwable)
            commandProcessor.close(throwable)
            stateHolder.close(throwable)
        }
        return job
    }

    override suspend fun issue(command: Command<State>) {
        if (stage.get() == Stage.Stopped) {
            throw StoreStoppedException
        }
        commandProcessor.issue(command)
    }

    private enum class Stage { Initial, Started, Stopped }
}

private const val DEFAULT_BUFFER_SIZE = 64
