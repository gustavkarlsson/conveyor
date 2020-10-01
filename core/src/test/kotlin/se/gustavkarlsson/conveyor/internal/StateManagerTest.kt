package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

object StateManagerTest : Spek({
    val initialState = "initial"
    val state1 = "state1"

    describe("A manager") {
        val subject by memoized { StateManager(initialState) }

        it("currentState is initial") {
            expectThat(subject.currentState).isEqualTo(initialState)
        }
        it("state emits initial") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.state.toList()
                }
                deferred.cancel()
                deferred.await()
            }
            expectThat(result).containsExactly(initialState)
        }
        it("currentState is state1 after reducing it to state1") {
            subject.reduce { state1 }
            expectThat(subject.currentState).isEqualTo(state1)
        }
        it("state emits initial and state1 when reducing it to state1 after collecting") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.state.toList()
                }
                subject.reduce { state1 }
                deferred.cancel()
                deferred.await()
            }
            expectThat(result).containsExactly(initialState, state1)
        }
        it("state emits initial and ends when cancelling") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.state.single()
                }
                subject.cancel()
                deferred.await()
            }
            expectThat(result).isEqualTo(initialState)
        }

        describe("that was cancelled") {
            beforeEachTest {
                subject.cancel()
            }

            it("currentState is currentState") {
                val result = subject.currentState
                expectThat(result).isEqualTo(initialState)
            }
            it("setting currentState throws") {
                expectThrows<IllegalStateException> {
                    subject.reduce { "shouldThrow" }
                    Unit
                }
            }
            it("getting currentState after trying to set currentState returns initial state") {
                try {
                    subject.reduce { "shouldThrow" }
                } catch (ignore: Throwable) {}
                expectThat(subject.currentState).isEqualTo(initialState)
            }
            it("state emits initial and then ends") {
                val result = runBlockingTest {
                    subject.state.single()
                }
                expectThat(result).isEqualTo(initialState)
            }
            it("cancel is successful") {
                subject.cancel()
            }
        }
    }
})
