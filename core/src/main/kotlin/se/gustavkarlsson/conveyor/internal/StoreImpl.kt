package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.Store

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    private val stateAccess: StateAccess<State>,
    private val actionIssuer: ActionIssuer<State>,
    liveActionsCounter: LiveActionsCounter,
    private val actionProcessors: Iterable<ActionProcessor<State>>,
    private val cancellables: Iterable<Cancellable>,
) : Store<State> {
    override val state = stateAccess.flow
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

    // TODO Make sure the nested launches make sense
    private fun CoroutineScope.startProcessing(): Job = launch {
        for (processor in actionProcessors) {
            launch { processor.process(stateAccess) }
        }
        awaitCancellation()
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
