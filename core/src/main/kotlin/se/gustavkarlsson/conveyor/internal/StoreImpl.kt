package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.Transformer

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    stateFlowProvider: StateFlowProvider<State>,
    private val stateAccess: StateAccess<State>,
    private val actionIssuer: ActionIssuer<State>,
    liveActionsCounter: LiveActionsCounter,
    private val actionProcessors: Iterable<ActionProcessor<State>>,
    private val actionTransformers: Iterable<Transformer<Action<State>>>,
    private val cancellables: Iterable<Cancellable>,
) : Store<State> {
    override val state = stateFlowProvider.stateFlow
        .onStart { liveActionsCounter.increment() }
        .onCompletion { liveActionsCounter.decrement() }

    override val currentState get() = stateAccess.get()

    private val stage = Stage()

    override fun start(scope: CoroutineScope): Job {
        stage.start()
        val job = scope.startProcessing()
        job.invokeOnCompletion(::stop)
        return job
    }

    private fun CoroutineScope.startProcessing(): Job = launch {
        for (processor in actionProcessors) {
            launch {
                processor.process { action ->
                    launch { action.execute(stateAccess) }
                }
            }
        }
    }

    private fun stop(throwable: Throwable?) {
        stage.stop(throwable)
        for (cancellable in cancellables) {
            cancellable.cancel(throwable)
        }
    }

    override fun issue(action: Action<State>) {
        stage.requireStarted()
        actionIssuer.issue(action)
    }
}
