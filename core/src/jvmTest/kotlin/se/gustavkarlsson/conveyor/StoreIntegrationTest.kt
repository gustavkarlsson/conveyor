package se.gustavkarlsson.conveyor

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.NullAction
import se.gustavkarlsson.conveyor.testing.SetStateAction
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

object StoreIntegrationTest : Spek({
    val initialState = "initial"
    val state1 = "state1"
    val fixedStateAction1 = SetStateAction(state1)
    val action = NullAction<String>()

    describe("A minimal store") {
        val subject by memoized {
            Store(initialState)
        }

        it("state emits initial") {
            runTest {
                val result = subject.state.first()
                expectThat(result).isEqualTo(initialState)
                cancel()
            }
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
            val runTest: (testBody: suspend TestScope.(Job) -> Unit) -> Unit by memoized {
                { testBody ->
                    runTest {
                        val job = launch { subject.run() }
                        testBody(job)
                    }
                }
            }

            it("has an active job") {
                runTest { job ->
                    expectThat(job.isActive).isTrue()
                    cancel()
                }
            }
            /*
            FIXME can't get this to run
            it("throws exception when started") {
                runTest {
                    runCurrent()
                    expectThrows<StoreAlreadyStartedException> {
                        launch { subject.run() }
                        runCurrent()
                    }
                    cancel()
                }
            }
            */
            it("state emits initial") {
                runTest {
                    runCurrent()
                    val result = subject.state.first()
                    expectThat(result).isEqualTo(initialState)
                    cancel()
                }
            }

            describe("and had its job explicitly cancelled") {
                val runTest: (testBody: suspend TestScope.(Job) -> Unit) -> Unit by memoized {
                    { testBody ->
                        runTest { job ->
                            job.cancel("Purposefully cancelled before test")
                            testBody(job)
                        }
                    }
                }
                /*
                FIXME can't get this to run
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
                */
                it("state.value returns initial") {
                    runTest {
                        val result = subject.state.value
                        expectThat(result).isEqualTo(initialState)
                    }
                }
                it("state emits initial") {
                    runTest {
                        val result = subject.state.first()
                        expectThat(result).isEqualTo(initialState)
                    }
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
