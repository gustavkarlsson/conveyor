package se.gustavkarlsson.conveyor.internal

import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException

internal class StageManager {
    private var stage: Stage = Stage.NotYetStarted

    @Synchronized
    fun start() {
        stage = when (val stage = stage) {
            Stage.NotYetStarted -> Stage.Started
            is Stage.Started -> throw StoreAlreadyStartedException()
            is Stage.Stopped -> throw StoreStoppedException(stage.cancellationReason)
        }
    }

    @Synchronized
    fun stop(cancellationReason: Throwable?) {
        stage = when (val stage = stage) {
            Stage.NotYetStarted -> throw StoreNotYetStartedException()
            is Stage.Started -> Stage.Stopped(cancellationReason)
            is Stage.Stopped -> throw StoreStoppedException(stage.cancellationReason)
        }
    }

    fun requireStarted() =
        when (val stage = stage) {
            Stage.NotYetStarted -> throw StoreNotYetStartedException()
            is Stage.Started -> Unit
            is Stage.Stopped -> throw StoreStoppedException(stage.cancellationReason)
        }

    private sealed class Stage {
        object NotYetStarted : Stage()
        object Started : Stage()
        data class Stopped(val cancellationReason: Throwable?) : Stage()
    }
}
