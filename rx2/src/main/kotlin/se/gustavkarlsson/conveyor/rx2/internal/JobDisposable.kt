package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

internal class JobDisposable(
    private val job: Job,
) : Disposable {
    override fun dispose() = job.cancel("JobDisposable disposed")

    override fun isDisposed(): Boolean = job.isCompleted
}
