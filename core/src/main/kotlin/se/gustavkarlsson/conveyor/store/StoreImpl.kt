package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.StoreClosedException
import se.gustavkarlsson.conveyor.StoreOpenedException
import java.util.concurrent.atomic.AtomicReference

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    private val stateHolder: StateHolder<State>,
    private val commandProcessor: CommandProcessor<State>,
    private val commandIssuer: CommandIssuer<State>,
    private val startActionsProcessor: StartActionsProcessor,
    private val liveActionsProcessor: LiveActionsProcessor,
) : Store<State> {
    override val state = stateHolder.flow
        .onStart { liveActionsProcessor.increaseLiveCount() }
        .onCompletion { liveActionsProcessor.decreaseLiveCount() }

    override val currentState get() = stateHolder.get()

    private val stage = AtomicReference<Stage>(Stage.Initial)

    override fun open(scope: CoroutineScope): Job {
        if (!stage.compareAndSet(Stage.Initial, Stage.Opened)) {
            throw StoreOpenedException
        }
        val job = scope.launch {
            launch { commandProcessor.process(scope) }
            launch { startActionsProcessor.process(scope) }
            launch { liveActionsProcessor.process(scope) }
        }
        job.invokeOnCompletion { throwable ->
            stage.set(Stage.Closed(throwable))
            liveActionsProcessor.cancel(throwable)
            commandProcessor.cancel(throwable)
            stateHolder.cancel(throwable)
        }
        return job
    }

    override suspend fun issue(command: Command<State>) {
        val currentStage = stage.get()
        if (currentStage is Stage.Closed) {
            throw StoreClosedException(currentStage.cause)
        }
        commandIssuer.issue(command)
    }

    private sealed class Stage {
        object Initial : Stage()
        object Opened : Stage()
        data class Closed(val cause: Throwable?) : Stage()
    }
}
