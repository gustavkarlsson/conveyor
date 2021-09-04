package se.gustavkarlsson.conveyor.internal

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException
import strikt.api.expectThrows
import strikt.assertions.isSameInstanceAs

object StageManagerTest : Spek({
    describe("A StageManager") {
        val subject by memoized { StageManager() }

        it("stop throws exception") {
            expectThrows<StoreNotYetStartedException> {
                subject.stop(Throwable())
            }
        }
        it("requireStarted throws exception") {
            expectThrows<StoreNotYetStartedException> {
                subject.requireStarted()
            }
        }

        describe("that was started") {
            beforeEachTest { subject.start() }

            it("start throws exception") {
                expectThrows<StoreAlreadyStartedException> {
                    subject.start()
                }
            }
            it("requireStarted succeeds") {
                subject.requireStarted()
            }
            it("stop succeeds") {
                subject.stop(Throwable())
            }

            describe("that was stopped") {
                val reason = Exception()
                beforeEachTest { subject.stop(reason) }

                it("start throws exception") {
                    expectThrows<StoreStoppedException> {
                        subject.start()
                    }.get { cancellationReason }.isSameInstanceAs(reason)
                }
                it("requireStarted throws exception") {
                    expectThrows<StoreStoppedException> {
                        subject.requireStarted()
                    }.get { cancellationReason }.isSameInstanceAs(reason)
                }
                it("stop throws exception") {
                    expectThrows<StoreStoppedException> {
                        subject.stop(Throwable())
                    }.get { cancellationReason }.isSameInstanceAs(reason)
                }
            }
        }
    }
})
