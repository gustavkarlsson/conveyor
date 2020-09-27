package se.gustavkarlsson.conveyor.internal

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
    private val stateContainer: ReadableStateContainer<State>,
    private val commandIssuer: CommandIssuer<State>,
    liveActionsCounter: LiveActionsCounter,
    private val processors: Iterable<Processor<State>>,
    private val cancellables: Iterable<Cancellable>,
) : Store<State> {
    override val state = stateContainer.state
        .onStart { liveActionsCounter.increment() }
        .onCompletion { liveActionsCounter.decrement() }

    override val currentState get() = stateContainer.currentState

    private val stage = AtomicReference<Stage>(Stage.Initial)

    override fun open(scope: CoroutineScope): Job {
        // TODO Does getAndUpdate work on Android?
        stage.getAndUpdate { current ->
            when (current) {
                Stage.Initial -> Stage.Opened
                Stage.Opened -> throw StoreOpenedException
                is Stage.Closed -> throw StoreClosedException(current.cause)
            }
        }
        val job = scope.launch {
            for (processor in processors) {
                launch {
                    processor.process { action ->
                        launch { action.execute(commandIssuer) }
                    }
                }
            }
        }
        job.invokeOnCompletion { throwable ->
            stage.set(Stage.Closed(throwable))
            for (cancellable in cancellables) {
                cancellable.cancel(throwable)
            }
        }
        return job
    }

    override fun issue(command: Command<State>) {
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
