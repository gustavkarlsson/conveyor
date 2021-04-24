package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Store

internal class StoreImpl<State>(
    stateFlow: StateFlow<State>,
    private val actionIssuer: ActionIssuer<State>,
    private val processes: Iterable<Process>,
) : Store<State> {
    override val state = stateFlow

    private val stageManager = StageManager()

    override var job: Job? = null
        private set

    override fun start(scope: CoroutineScope): Job {
        stageManager.start()
        val job = Job(scope.coroutineContext[Job])
        val context = Dispatchers.Unconfined + CoroutineName("Store") + job
        for (process in processes) {
            scope.launch(context) { process.run() }
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
