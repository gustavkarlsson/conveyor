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
import se.gustavkarlsson.conveyor.StoreCancelledException
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    stateFlowProvider: StateFlowProvider<State>,
    private val stateAccess: StateAccess<State>,
    private val actionIssuer: ActionIssuer<State>,
    liveActionsCounter: LiveActionsCounter,
    private val actionProcessors: Iterable<ActionProcessor<State>>,
    private val cancellables: Iterable<Cancellable>,
) : Store<State> {
    override val state = stateFlowProvider.stateFlow
        .onStart { liveActionsCounter.increment() }
        .onCompletion { liveActionsCounter.decrement() }

    override val currentState get() = stateAccess.currentState

    private var stage: Stage = Stage.NotYetStarted

    override fun start(scope: CoroutineScope): Job {
        setStart()
        return scope.startProcessing()
            .also { it.invokeOnCompletion(::cancel) }
    }

    private fun setStart() {
        synchronized(stage) {
            when (val currentStage = stage) {
                Stage.NotYetStarted -> Stage.Started
                Stage.Started -> throw StoreAlreadyStartedException()
                is Stage.Cancelled -> throw StoreCancelledException(currentStage.reason)
            }
        }
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

    private fun cancel(throwable: Throwable?) {
        synchronized(stage) { stage = Stage.Cancelled(throwable) }
        for (cancellable in cancellables) {
            cancellable.cancel(throwable)
        }
    }

    override fun issue(action: Action<State>) {
        synchronized(stage) {
            when (val currentStage = stage) {
                is Stage.NotYetStarted -> throw StoreNotYetStartedException()
                is Stage.Started -> Unit
                is Stage.Cancelled -> throw StoreCancelledException(currentStage.reason)
            }
        }
        actionIssuer.issue(action)
    }

    private sealed class Stage {
        object NotYetStarted : Stage()
        object Started : Stage()
        data class Cancelled(val reason: Throwable?) : Stage()
    }
}
