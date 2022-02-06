package se.gustavkarlsson.conveyor.internal

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException

class StageManagerTest : FunSpec({
    val subject = StageManager()

    test("stop throws exception") {
        shouldThrow<StoreNotYetStartedException> {
            subject.stop(Throwable())
        }
    }
    test("requireStarted throws exception") {
        shouldThrow<StoreNotYetStartedException> {
            subject.requireStarted()
        }
    }

    test("when started, start throws exception") {
        shouldThrow<StoreAlreadyStartedException> {
            subject.start()
            subject.start()
        }
    }

    test("when started, requireStarted succeeds") {
        subject.start()
        subject.requireStarted()
    }

    test("when started, stop succeeds") {
        subject.start()
        subject.stop(Throwable())
    }

    test("when stopped, start throws exception") {
        val reason = Throwable()
        subject.start()
        subject.stop(reason)
        shouldThrow<StoreStoppedException> {
            subject.start()
        }.cancellationReason.shouldBeSameInstanceAs(reason)
    }

    test("when stopped, requireStarted throws exception") {
        val reason = Throwable()
        subject.start()
        subject.stop(reason)
        shouldThrow<StoreStoppedException> {
            subject.requireStarted()
        }.cancellationReason.shouldBeSameInstanceAs(reason)
    }

    test("when stopped, stop throws exception") {
        val reason = Throwable()
        subject.start()
        subject.stop(reason)
        shouldThrow<StoreStoppedException> {
            subject.stop(Throwable())
        }.cancellationReason.shouldBeSameInstanceAs(reason)
    }
})
