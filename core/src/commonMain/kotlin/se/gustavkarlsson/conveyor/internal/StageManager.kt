package se.gustavkarlsson.conveyor.internal

import kotlin.jvm.Volatile
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException

internal class StageManager {
    @Volatile
    private var stage: Stage = Stage.NotYetStarted

    fun start() {
        synchronized(this) {
            stage = when (val stage = stage) {
                Stage.NotYetStarted -> Stage.Started
                is Stage.Started -> throw StoreAlreadyStartedException()
                is Stage.Stopped -> throw StoreStoppedException(stage.cancellationReason)
            }
        }
    }

    fun stop(cancellationReason: Throwable) {
        synchronized(this) {
            stage = when (val stage = stage) {
                Stage.NotYetStarted -> throw StoreNotYetStartedException()
                is Stage.Started -> Stage.Stopped(cancellationReason)
                is Stage.Stopped -> throw StoreStoppedException(stage.cancellationReason)
            }
        }
    }

    fun requireStarted() =
        when (val stage = stage) {
            Stage.NotYetStarted -> throw StoreNotYetStartedException()
            is Stage.Started -> Unit
            is Stage.Stopped -> throw StoreStoppedException(stage.cancellationReason)
        }

    private sealed interface Stage {
        object NotYetStarted : Stage
        object Started : Stage
        data class Stopped(val cancellationReason: Throwable) : Stage
    }
}
