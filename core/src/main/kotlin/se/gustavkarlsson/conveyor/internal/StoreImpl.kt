package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.Store

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    private val stateAccess: UpdatableStateFlow<State>,
    private val actionManager: ActionManager<State>,
) : Store<State> {
    override val state = stateAccess

    private val stage = Stage()

    override fun start(scope: CoroutineScope): Job {
        stage.start()
        val job = scope.startProcessing()
        job.invokeOnCompletion(::stop)
        return job
    }

    private fun CoroutineScope.startProcessing(): Job = launch {
        launch { actionManager.process(stateAccess) }
        awaitCancellation()
    }

    private fun stop(throwable: Throwable?) {
        stage.stop(throwable)
        actionManager.cancel(throwable)
    }

    override fun issue(action: Action<State>) {
        stage.requireStarted()
        actionManager.issue(action)
    }
}
