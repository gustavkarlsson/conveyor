package se.gustavkarlsson.conveyor.internal

import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException

internal class Stage {
    private var current: Stage = Stage.NotYetStarted

    @Synchronized
    fun start() {
        current = when (val current = current) {
            Stage.NotYetStarted -> Stage.Started
            Stage.Started -> throw StoreAlreadyStartedException()
            is Stage.Stopped -> throw StoreStoppedException(current.cancellationReason)
        }
    }

    fun stop(cancellationReason: Throwable?) {
        current = Stage.Stopped(cancellationReason)
    }

    fun requireStarted() =
        when (val current = current) {
            Stage.NotYetStarted -> throw StoreNotYetStartedException()
            Stage.Started -> Unit
            is Stage.Stopped -> throw StoreStoppedException(current.cancellationReason)
        }

    private sealed class Stage {
        object NotYetStarted : Stage()
        object Started : Stage()
        data class Stopped(val cancellationReason: Throwable?) : Stage()
    }
}
