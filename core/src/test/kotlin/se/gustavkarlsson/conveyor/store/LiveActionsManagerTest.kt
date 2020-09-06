package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.actions.VoidAction
import se.gustavkarlsson.conveyor.test.NullAction
import se.gustavkarlsson.conveyor.test.TrackingCommandIssuer
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.util.concurrent.atomic.AtomicInteger

object LiveActionsManagerTest : Spek({
    val executedActions by memoized { mutableListOf<Action<String>>() }
    val nullAction = NullAction<String>()

    describe("A manager with a single null action") {
        val subject by memoized { LiveActionsManager(listOf(nullAction)) }

        it("throws exception if live count is decreased") {
            expectThrows<IllegalStateException> {
                runBlockingTest {
                    subject.decrement()
                }
            }
        }
        it("processing does nothing") {
            runBlockingTest {
                val job = launch {
                    subject.process { executedActions += it }
                }
                job.cancel()
            }
            expectThat(executedActions).isEmpty()
        }

        describe("that was incremented once") {
            beforeEachTest {
                subject.increment()
            }

            it("processing executes action") {
                runBlockingTest {
                    val job = launch {
                        subject.process { executedActions += it }
                    }
                    job.cancel()
                }
                expectThat(executedActions).containsExactly(nullAction)
            }

            it("processing while decrementing back to 0 and incrementing again executes action twice") {
                runBlockingTest {
                    val job = launch {
                        subject.process { executedActions += it }
                    }
                    subject.decrement()
                    subject.increment()
                    job.cancel()
                }
                expectThat(executedActions).containsExactly(nullAction, nullAction)
            }

            describe("and then decremented once") {
                beforeEachTest {
                    subject.decrement()
                }

                it("processing does nothing") {
                    runBlockingTest {
                        val job = launch {
                            subject.process { executedActions += it }
                        }
                        job.cancel()
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

            it("processing executes action") {
                runBlockingTest {
                    val job = launch {
                        subject.process { executedActions += it }
                    }
                    job.cancel()
                }
                expectThat(executedActions).containsExactly(nullAction)
            }

            describe("and then decremented once") {
                beforeEachTest {
                    subject.decrement()
                }

                it("processing executes action") {
                    runBlockingTest {
                        val job = launch {
                            subject.process { executedActions += it }
                        }
                        job.cancel()
                    }
                    expectThat(executedActions).containsExactly(nullAction)
                }
            }
        }
    }
    describe("A manager with two delayed actions that was incremented once") {
        val counter by memoized { AtomicInteger(0) }
        val delayAction10 = VoidAction<String> {
            delay(10)
            counter.incrementAndGet()
        }
        val subject by memoized { LiveActionsManager(listOf(delayAction10, delayAction10)) }
        beforeEachTest { subject.increment() }

        it("stops executing after decrementing") {
            val commandIssuer = TrackingCommandIssuer<String>()
            runBlockingTest {
                val job = launch {
                    subject.process { it.execute(commandIssuer) }
                }
                expectThat(counter.get()).isEqualTo(0)
                advanceTimeBy(10)
                expectThat(counter.get()).isEqualTo(1)
                subject.decrement()
                advanceTimeBy(10)
                expectThat(counter.get()).isEqualTo(1)
                job.cancel()
            }
        }
    }
})
