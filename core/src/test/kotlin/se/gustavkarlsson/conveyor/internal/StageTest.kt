package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.Job
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

object StageTest : Spek({
    describe("A Stage") {
        val cancellationReason = Exception()
        val subject by memoized { Stage() }

        it("setJob throws exception") {
            expectThrows<StoreNotYetStartedException> {
                subject.setJob(Job())
            }
        }
        it("stop throws exception") {
            expectThrows<StoreNotYetStartedException> {
                subject.stop(null)
            }
        }
        it("requireActive throws exception") {
            expectThrows<StoreNotYetStartedException> {
                subject.requireActive()
            }
        }
        it("has no job") {
            expectThat(subject.job).isNull()
        }

        describe("that is starting") {
            beforeEachTest { subject.start() }

            it("start throws exception") {
                expectThrows<StoreAlreadyStartedException> {
                    subject.start()
                }
            }
            it("requireActive throws exception") {
                expectThrows<StoreNotYetStartedException> {
                    subject.requireActive()
                }
            }
            it("stop throws exception") {
                expectThrows<StoreNotYetStartedException> {
                    subject.stop(null)
                }
            }
            it("has no job") {
                expectThat(subject.job).isNull()
            }

            describe("that had its job set") {
                val job by memoized { Job() }
                beforeEachTest { subject.setJob(job) }

                it("start throws exception") {
                    expectThrows<StoreAlreadyStartedException> {
                        subject.start()
                    }
                }
                it("setJob throws exception") {
                    expectThrows<StoreAlreadyStartedException> {
                        subject.setJob(Job())
                    }
                }
                it("requireActive succeeds") {
                    subject.requireActive()
                }
                it("has a job") {
                    expectThat(subject.job).isNotNull()
                }

                describe("that was stopped with a reason") {
                    beforeEachTest { subject.stop(cancellationReason) }

                    it("start throws exception") {
                        expectThrows<StoreStoppedException> {
                            subject.start()
                        }.get { this.cancellationReason }.isSameInstanceAs(cancellationReason)
                    }
                    it("setJob throws exception") {
                        expectThrows<StoreStoppedException> {
                            subject.setJob(Job())
                        }.get { this.cancellationReason }.isSameInstanceAs(cancellationReason)
                    }
                    it("requireActive throws exception") {
                        expectThrows<StoreStoppedException> {
                            subject.requireActive()
                        }.get { this.cancellationReason }.isSameInstanceAs(cancellationReason)
                    }
                    it("stop throws exception") {
                        expectThrows<StoreStoppedException> {
                            subject.stop(null)
                        }.get { this.cancellationReason }.isSameInstanceAs(cancellationReason)
                    }
                    it("has a job") {
                        expectThat(subject.job).isNotNull()
                    }
                }

                describe("that was stopped without a reason") {
                    beforeEachTest { subject.stop(null) }

                    it("start throws exception") {
                        expectThrows<StoreStoppedException> {
                            subject.start()
                        }.get { this.cancellationReason }.isNull()
                    }
                    it("setJob throws exception") {
                        expectThrows<StoreStoppedException> {
                            subject.setJob(Job())
                        }.get { this.cancellationReason }.isNull()
                    }
                    it("requireActive throws exception") {
                        expectThrows<StoreStoppedException> {
                            subject.requireActive()
                        }.get { this.cancellationReason }.isNull()
                    }
                    it("stop throws exception") {
                        expectThrows<StoreStoppedException> {
                            subject.stop(cancellationReason)
                        }.get { this.cancellationReason }.isNull()
                    }
                    it("has a job") {
                        expectThat(subject.job).isNotNull()
                    }
                }
            }
        }
    }
})
