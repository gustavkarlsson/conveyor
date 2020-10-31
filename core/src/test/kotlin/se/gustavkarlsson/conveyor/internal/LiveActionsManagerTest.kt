package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.action
import se.gustavkarlsson.conveyor.test.NullAction
import se.gustavkarlsson.conveyor.test.SimpleStateAccess
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.message
import java.util.concurrent.atomic.AtomicInteger

object LiveActionsManagerTest : Spek({
    val cancellationException = CancellationException("Manually cancelled")
    val executedActions by memoized { mutableListOf<Action<String>>() }
    val nullAction = NullAction<String>()

    describe("A LiveActionsManager with a single null action") {
        val subject by memoized { LiveActionsManager(listOf(nullAction)) }

        it("decrease throws exception") {
            expectThrows<IllegalStateException> {
                subject.decrement()
            }
        }
        it("collecting actionFlow gets nothing") {
            runBlockingTest {
                val job = launch {
                    subject.actionFlow.collect { executedActions += it }
                }
                job.cancel("Cancelled to end collecting")
            }
            expectThat(executedActions).isEmpty()
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
            it("collecting actionFlow throws exception") {
                expectThrows<CancellationException> {
                    subject.actionFlow.collect {}
                }.and { message.isEqualTo(cancellationException.message) }
            }
        }

        describe("that was incremented once") {
            beforeEachTest {
                subject.increment()
            }

            it("collecting actionFlow gets action") {
                runBlockingTest {
                    val job = launch {
                        subject.actionFlow.collect { executedActions += it }
                    }
                    job.cancel("Cancelled to end collecting")
                }
                expectThat(executedActions).containsExactly(nullAction)
            }

            it("decrementing back to 0 and incrementing again while collecting actionFlow gets action twice") {
                runBlockingTest {
                    val job = launch {
                        subject.actionFlow.collect { executedActions += it }
                    }
                    subject.decrement()
                    subject.increment()
                    job.cancel("Cancelled to end collecting")
                }
                expectThat(executedActions).containsExactly(nullAction, nullAction)
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

                it("collecting actionFlow gets nothing") {
                    runBlockingTest {
                        val job = launch {
                            subject.actionFlow.collect { executedActions += it }
                        }
                        job.cancel("Cancelled to end collecting")
                    }
                    expectThat(executedActions).isEmpty()
                }
            }
        }

        describe("that was incremented twice") {
            beforeEachTest {
                subject.increment()
                subject.increment()
            }

            it("collecting actionFlow gets action") {
                runBlockingTest {
                    val job = launch {
                        subject.actionFlow.collect { executedActions += it }
                    }
                    job.cancel("Cancelled to end collecting")
                }
                expectThat(executedActions).containsExactly(nullAction)
            }

            describe("and then decremented once") {
                beforeEachTest {
                    subject.decrement()
                }

                it("collecting actionFlow gets action") {
                    runBlockingTest {
                        val job = launch {
                            subject.actionFlow.collect { executedActions += it }
                        }
                        job.cancel("Cancelled to end collecting")
                    }
                    expectThat(executedActions).containsExactly(nullAction)
                }
            }
        }
    }
    describe("A LiveActionsManager with two delayed actions that was incremented once") {
        val counter by memoized { AtomicInteger(0) }
        val delayAction1 = action<String> {
            delay(1)
            counter.incrementAndGet()
        }
        val delayAction2 = action<String> {
            delay(1)
            counter.incrementAndGet()
            counter.incrementAndGet()
        }
        val subject by memoized { LiveActionsManager(listOf(delayAction1, delayAction2)) }
        beforeEachTest { subject.increment() }

        it("stops executing after decrementing") {
            val stateAccess = SimpleStateAccess("")
            runBlockingTest {
                val job = launch {
                    subject.actionFlow.collect { it.execute(stateAccess) }
                }
                expectThat(counter.get()).isEqualTo(0)
                advanceTimeBy(1)
                expectThat(counter.get()).isEqualTo(1)
                subject.decrement()
                advanceTimeBy(1)
                expectThat(counter.get()).isEqualTo(1)
                job.cancel("Cancelled to end collecting")
            }
        }

        it("executes actions sequentially") {
            val stateAccess = SimpleStateAccess("")
            runBlockingTest {
                val job = launch {
                    subject.actionFlow.collect { it.execute(stateAccess) }
                }
                expectThat(counter.get()).isEqualTo(0)
                advanceTimeBy(1)
                expectThat(counter.get()).isEqualTo(1)
                advanceTimeBy(1)
                expectThat(counter.get()).isEqualTo(3)
                job.cancel("Cancelled to end collecting")
            }
        }
    }
})
