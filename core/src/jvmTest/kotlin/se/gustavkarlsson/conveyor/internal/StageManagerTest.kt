package se.gustavkarlsson.conveyor.internal

import io.kotest.core.spec.style.FunSpec
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException
import strikt.api.expectThrows
import strikt.assertions.isSameInstanceAs

class StageManagerTest : FunSpec({
    val subject = StageManager()

    test("stop throws exception") {
        expectThrows<StoreNotYetStartedException> {
            subject.stop(Throwable())
        }
    }
    test("requireStarted throws exception") {
        expectThrows<StoreNotYetStartedException> {
            subject.requireStarted()
        }
    }

    test("when started, start throws exception") {
        expectThrows<StoreAlreadyStartedException> {
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
        expectThrows<StoreStoppedException> {
            subject.start()
        }.get { reason }.isSameInstanceAs(reason)
    }

    test("when stopped, requireStarted throws exception") {
        val reason = Throwable()
        subject.start()
        subject.stop(reason)
        expectThrows<StoreStoppedException> {
            subject.requireStarted()
        }.get { cancellationReason }.isSameInstanceAs(reason)
    }

    test("when stopped, stop throws exception") {
        val reason = Throwable()
        subject.start()
        subject.stop(reason)
        expectThrows<StoreStoppedException> {
            subject.stop(Throwable())
        }.get { cancellationReason }.isSameInstanceAs(reason)
    }
})
