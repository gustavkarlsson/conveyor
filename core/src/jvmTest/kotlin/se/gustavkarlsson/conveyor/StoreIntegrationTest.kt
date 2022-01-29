package se.gustavkarlsson.conveyor

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.NullAction
import se.gustavkarlsson.conveyor.testing.SetStateAction
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope
import se.gustavkarlsson.conveyor.testing.runTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

object StoreIntegrationTest : Spek({
    val initialState = "initial"
    val state1 = "state1"
    val fixedStateAction1 = SetStateAction(state1)
    val action = NullAction<String>()
    val scope by memoizedTestCoroutineScope()

    describe("A minimal store") {
        val subject by memoized {
            Store(initialState)
        }

        it("state emits initial") {
            val result = runTest {
                subject.state.first()
            }
            expectThat(result).isEqualTo(initialState)
        }
        it("state.value returns initial") {
            val result = subject.state.value
            expectThat(result).isEqualTo(initialState)
        }
        it("throws when issuing action") {
            expectThrows<StoreNotYetStartedException> {
                subject.issue(fixedStateAction1)
            }
        }

        describe("that was started") {
            lateinit var job: Job
            beforeEachTest {
                job = scope.launch { subject.run() }
            }

            it("has an active job") {
                expectThat(job.isActive).isTrue()
            }
            it("throws exception when started") {
                expectThrows<StoreAlreadyStartedException> {
                    subject.run()
                }
            }
            it("has its job cancelled after its scope was cancelled") {
                scope.cancel("Cancelling scope to test job cancellation")
                expectThat(job.isCancelled).isTrue()
            }
            it("state emits initial") {
                val result = runTest {
                    subject.state.first()
                }
                expectThat(result).isEqualTo(initialState)
            }

            describe("and had its job explicitly cancelled") {
                beforeEachTest {
                    job.cancel("Purposefully cancelled before test")
                }

                it("throws exception when started") {
                    expectThrows<StoreStoppedException> {
                        subject.run()
                    }
                }
                it("throws exception when an action is issued") {
                    expectThrows<StoreStoppedException> {
                        subject.issue(action)
                    }
                }
                it("state.value returns initial") {
                    val result = subject.state.value
                    expectThat(result).isEqualTo(initialState)
                }
                it("state emits initial") {
                    val result = runTest {
                        subject.state.first()
                    }
                    expectThat(result).isEqualTo(initialState)
                }
            }
        }
    }
    describe("A store with a starting action that fails when executed") {
        val failingAction = Action<String> { error("failed") }
        val subject by memoized {
            Store(initialState, startActions = listOf(failingAction))
        }

        it("cancels when started") {
            expectThrows<IllegalStateException> {
                subject.run()
            }
        }
    }
    describe("A store with a starting action that fails when updating") {
        val failingAction = Action<String> { storeFlow ->
            storeFlow.update { error("failed") }
        }
        val subject by memoized {
            Store(initialState, startActions = listOf(failingAction))
        }

        it("cancels when started") {
            expectThrows<IllegalStateException> {
                subject.run()
            }
        }
    }
})
