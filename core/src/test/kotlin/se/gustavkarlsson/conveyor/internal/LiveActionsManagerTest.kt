package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.action
import se.gustavkarlsson.conveyor.test.IncrementStateAction
import se.gustavkarlsson.conveyor.test.SimpleStateAccess
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.message

object LiveActionsManagerTest : Spek({
    val cancellationException = CancellationException("Manually cancelled")
    val stateAccess by memoized { SimpleStateAccess(0) }
    val incrementStateAction = IncrementStateAction()

    describe("A LiveActionsManager with a single null action") {
        val subject by memoized { LiveActionsManager(listOf(incrementStateAction)) }

        it("decrease throws exception") {
            expectThrows<IllegalStateException> {
                subject.decrement()
            }
        }
        it("processing doesn't execute any actions") {
            runBlockingTest {
                val job = launch {
                    subject.process(stateAccess)
                }
                job.cancel("Cancelled to end processing")
            }
            expectThat(stateAccess.state.value).isEqualTo(0)
        }

        describe("that was cancelled") {
            beforeEachTest {
                subject.cancel(cancellationException)
            }

            it("increment throws exception") {
                expectThrows<IllegalStateException> {
                    subject.increment()
                }
            }
            it("can cancel again") {
                subject.cancel(cancellationException)
            }
            it("processing throws exception") {
                expectThrows<CancellationException> {
                    subject.process(stateAccess)
                }.message.isEqualTo(cancellationException.message)
            }
        }

        describe("that was incremented once") {
            beforeEachTest {
                subject.increment()
            }

            it("processing executes action") {
                runBlockingTest {
                    val job = launch {
                        subject.process(stateAccess)
                    }
                    job.cancel("Cancelled to end processing")
                }
                expectThat(stateAccess.state.value).isEqualTo(1)
            }

            it("decrementing and incrementing again while processing executes action twice") {
                runBlockingTest {
                    val job = launch {
                        subject.process(stateAccess)
                    }
                    subject.decrement()
                    subject.increment()
                    job.cancel("Cancelled to end processing")
                }
                expectThat(stateAccess.state.value).isEqualTo(2)
            }

            describe("that was cancelled") {
                beforeEachTest {
                    subject.cancel(cancellationException)
                }

                it("decrement throws exception") {
                    expectThrows<IllegalStateException> {
                        subject.decrement()
                    }
                }
            }

            describe("and then decremented once") {
                beforeEachTest {
                    subject.decrement()
                }

                it("processing doesn't execute any actions") {
                    runBlockingTest {
                        val job = launch {
                            subject.process(stateAccess)
                        }
                        job.cancel("Cancelled to end processing")
                    }
                    expectThat(stateAccess.state.value).isEqualTo(0)
                }
            }
        }

        describe("that was incremented twice") {
            beforeEachTest {
                subject.increment()
                subject.increment()
            }

            it("processing executes action") {
                runBlockingTest {
                    val job = launch {
                        subject.process(stateAccess)
                    }
                    job.cancel("Cancelled to end processing")
                }
                expectThat(stateAccess.state.value).isEqualTo(1)
            }

            describe("and then decremented once") {
                beforeEachTest {
                    subject.decrement()
                }

                it("processing executes action") {
                    runBlockingTest {
                        val job = launch {
                            subject.process(stateAccess)
                        }
                        job.cancel("Cancelled to end processing")
                    }
                    expectThat(stateAccess.state.value).isEqualTo(1)
                }
            }
        }
    }
    describe("A LiveActionsManager with a delayed action that was incremented once") {
        val delayAction = action<Int> { access ->
            delay(1)
            access.update { this + 1 }
        }
        val subject by memoized { LiveActionsManager(listOf(delayAction)) }
        beforeEachTest { subject.increment() }

        it("stops executing after decrementing") {
            runBlockingTest {
                val job = launch {
                    subject.process(stateAccess)
                }
                subject.decrement()
                advanceTimeBy(1)
                expectThat(stateAccess.state.value).isEqualTo(0)
                job.cancel("Cancelled to end processing")
            }
        }
    }
    describe("A LiveActionsManager with two delayed actions that was incremented once") {
        val delayAction = action<Int> { access ->
            delay(1)
            access.update { this + 1 }
        }
        val subject by memoized { LiveActionsManager(listOf(delayAction, delayAction)) }
        beforeEachTest { subject.increment() }

        it("executes actions in parallel") {
            runBlockingTest {
                val job = launch {
                    subject.process(stateAccess)
                }
                advanceTimeBy(1)
                expectThat(stateAccess.state.value).isEqualTo(2)
                job.cancel("Cancelled to end processing")
            }
        }
    }
})
