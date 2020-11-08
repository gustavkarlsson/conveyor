package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
    val subscriptionCount by memoized { MutableStateFlow(0) }

    describe("A LiveActionsManager with a single null action") {
        val subject by memoized { LiveActionsManager(listOf(incrementStateAction), subscriptionCount) }

        it("negative count causes error") {
            expectThrows<IllegalStateException> {
                runBlockingTest {
                    launch { subject.process(stateAccess) }
                    subscriptionCount.value = -1
                }
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

            it("can cancel again") {
                subject.cancel(cancellationException)
            }
            it("processing throws exception") {
                expectThrows<CancellationException> {
                    subject.process(stateAccess)
                }.message.isEqualTo(cancellationException.message)
            }
        }

        describe("that was set to 1") {
            beforeEachTest {
                subscriptionCount.value = 1
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

            it("setting to 0 and then 1 again while processing executes action twice") {
                runBlockingTest {
                    val job = launch {
                        subject.process(stateAccess)
                    }
                    subscriptionCount.value = 0
                    advanceUntilIdle() // TODO Figure out why this is necessary
                    subscriptionCount.value = 1
                    job.cancel("Cancelled to end processing")
                }
                expectThat(stateAccess.state.value).isEqualTo(2)
            }

            describe("and then set to 0") {
                beforeEachTest {
                    subscriptionCount.value = 0
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
    }
    describe("A LiveActionsManager with a delayed action that was set to 1") {
        val delayAction = action<Int> { access ->
            delay(1)
            access.update { this + 1 }
        }
        val subject by memoized { LiveActionsManager(listOf(delayAction), subscriptionCount) }
        beforeEachTest { subscriptionCount.value = 1 }

        it("stops executing after setting to 0") {
            runBlockingTest {
                val job = launch {
                    subject.process(stateAccess)
                }
                subscriptionCount.value = 0
                advanceTimeBy(1)
                expectThat(stateAccess.state.value).isEqualTo(0)
                job.cancel("Cancelled to end processing")
            }
        }
    }
    describe("A LiveActionsManager with two delayed actions that was set to 1") {
        val delayAction = action<Int> { access ->
            delay(1)
            access.update { this + 1 }
        }
        val subject by memoized { LiveActionsManager(listOf(delayAction, delayAction), subscriptionCount) }
        beforeEachTest { subscriptionCount.value = 1 }

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
