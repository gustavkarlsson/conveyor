package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

object StateManagerTest : Spek({
    val collected by memoized { mutableListOf<String>() }
    val initialState = "initial"
    val state1 = "state1"

    describe("A manager") {
        val subject by memoized { StateManager(initialState) }

        it("currentState is initial") {
            expectThat(subject.currentState).isEqualTo(initialState)
        }
        it("state emits initial") {
            runBlockingTest {
                val job = launch {
                    subject.state.collect {
                        collected += it
                    }
                }
                job.cancel()
            }
            expectThat(collected).containsExactly(initialState)
        }
        it("currentState is state1 after setting it to state1") {
            subject.currentState = state1
            expectThat(subject.currentState).isEqualTo(state1)
        }
        it("currentState is state1 after setting it to state1") {
            runBlockingTest {
                val job = launch {
                    subject.state.collect { collected += it }
                }
                subject.currentState = state1
                job.cancel()
            }
            expectThat(collected).containsExactly(initialState, state1)
        }

        describe("that was cancelled") {
            beforeEachTest {
                subject.cancel()
            }

            it("currentState throws") {
                expectThrows<IllegalStateException> {
                    subject.currentState
                }
            }
            it("state emits nothing") {
                val result = runBlockingTest {
                    subject.state.toList()
                }
                expectThat(result).isEmpty()
            }
            it("cancel is successful") {
                subject.cancel()
            }
        }
    }
})
