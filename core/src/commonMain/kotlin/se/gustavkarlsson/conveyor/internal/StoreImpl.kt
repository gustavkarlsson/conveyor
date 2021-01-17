package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Store

internal class StoreImpl<State>(
    stateFlow: StateFlow<State>,
    private val actionIssuer: ActionIssuer<State>,
    private val launchers: Iterable<Launcher>,
) : Store<State> {
    override val state = stateFlow

    private val stageManager = StageManager()

    override var job: Job? = null
        private set

    override fun start(scope: CoroutineScope): Job {
        stageManager.start()
        val job = scope.launch {
            launchers.map { launcher ->
                launcher.launch(scope)
            }.joinAll()
        }
        this.job = job
        job.invokeOnCompletion(::stop)
        return job
    }

    private fun stop(throwable: Throwable?) {
        stageManager.stop(throwable)
        actionIssuer.cancel(throwable)
    }

    override fun issue(action: Action<State>) {
        stageManager.requireStarted()
        actionIssuer.issue(action)
    }
}