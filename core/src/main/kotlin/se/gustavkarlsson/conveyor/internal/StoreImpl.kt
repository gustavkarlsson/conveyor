package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.StoreClosedException
import se.gustavkarlsson.conveyor.StoreOpenedException
import java.util.concurrent.atomic.AtomicReference

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    stateFlowProvider: StateFlowProvider<State>,
    private val stateAccess: StateAccess<State>,
    private val actionIssuer: ActionIssuer<State>,
    liveActionsCounter: LiveActionsCounter,
    private val processors: Iterable<Processor<State>>,
    private val cancellables: Iterable<Cancellable>,
) : Store<State> {
    override val state = stateFlowProvider.stateFlow
        .onStart { liveActionsCounter.increment() }
        .onCompletion { liveActionsCounter.decrement() }

    override val currentState get() = stateAccess.currentState

    private val stage = AtomicReference<Stage>(Stage.Initial)

    override fun open(scope: CoroutineScope): Job {
        // TODO Does getAndUpdate work on Android?
        stage.getAndUpdate { current ->
            when (current) {
                Stage.Initial -> Stage.Opened
                Stage.Opened -> throw StoreOpenedException()
                is Stage.Closed -> throw StoreClosedException(current.cause)
            }
        }
        val job = scope.launch {
            for (processor in processors) {
                launch {
                    processor.process { action ->
                        launch { action.execute(stateAccess) }
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

    override fun issue(action: Action<State>) {
        val currentStage = stage.get()
        if (currentStage is Stage.Closed) {
            throw StoreClosedException(currentStage.cause)
        }
        actionIssuer.issue(action)
    }

    private sealed class Stage {
        object Initial : Stage()
        object Opened : Stage()
        data class Closed(val cause: Throwable?) : Stage()
    }
}
