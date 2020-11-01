package se.gustavkarlsson.conveyor.internal

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException
import strikt.api.expectThrows
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

object StageTest : Spek({
    describe("A Stage") {
        val cancellationReason = Exception()
        val subject by memoized { Stage() }

        it("requireStarted throws exception") {
            expectThrows<StoreNotYetStartedException> {
                subject.requireStarted()
            }
        }
        it("stop succeeds") {
            subject.stop(null)
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

            describe("that was stopped with a reason") {
                beforeEachTest { subject.stop(cancellationReason) }

                it("start throws exception") {
                    expectThrows<StoreStoppedException> {
                        subject.start()
                    }.get { this.cancellationReason }.isSameInstanceAs(cancellationReason)
                }
                it("requireStarted throws exception") {
                    expectThrows<StoreStoppedException> {
                        subject.requireStarted()
                    }.get { this.cancellationReason }.isSameInstanceAs(cancellationReason)
                }
            }

            describe("that was stopped without a reason") {
                beforeEachTest { subject.stop(null) }

                it("start throws exception") {
                    expectThrows<StoreStoppedException> {
                        subject.start()
                    }.get { this.cancellationReason }.isNull()
                }
                it("requireStarted throws exception") {
                    expectThrows<StoreStoppedException> {
                        subject.requireStarted()
                    }.get { this.cancellationReason }.isNull()
                }
            }
        }
    }
})
