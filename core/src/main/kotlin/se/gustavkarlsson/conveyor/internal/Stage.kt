package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.Job
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException

internal class Stage {
    private var current: Stage = Stage.NotYetStarted

    // FIXME is job really necessary here?
    val job: Job? get() = current.job

    @Synchronized
    fun start() {
        current = when (val current = current) {
            Stage.NotYetStarted -> Stage.Starting
            Stage.Starting, is Stage.Active -> throw StoreAlreadyStartedException()
            is Stage.Stopped -> throw StoreStoppedException(current.cancellationReason)
        }
    }

    @Suppress("ThrowsCount")
    @Synchronized
    fun setJob(job: Job) {
        current = when (val current = current) {
            Stage.NotYetStarted -> throw StoreNotYetStartedException()
            Stage.Starting -> Stage.Active(job)
            is Stage.Active -> throw StoreAlreadyStartedException()
            is Stage.Stopped -> throw StoreStoppedException(current.cancellationReason)
        }
    }

    @Synchronized
    fun stop(cancellationReason: Throwable?) {
        current = when (val current = current) {
            Stage.NotYetStarted, Stage.Starting -> throw StoreNotYetStartedException()
            is Stage.Active -> Stage.Stopped(current.job, cancellationReason)
            is Stage.Stopped -> throw StoreStoppedException(current.cancellationReason)
        }
    }

    fun requireActive() =
        when (val current = current) {
            Stage.NotYetStarted, Stage.Starting -> throw StoreNotYetStartedException()
            is Stage.Active -> Unit
            is Stage.Stopped -> throw StoreStoppedException(current.cancellationReason)
        }

    private sealed class Stage {
        abstract val job: Job?

        object NotYetStarted : Stage() {
            override val job: Nothing? = null
        }

        object Starting : Stage() {
            override val job: Nothing? = null
        }

        data class Active(override val job: Job) : Stage()
        data class Stopped(override val job: Job, val cancellationReason: Throwable?) : Stage()
    }
}
