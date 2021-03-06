package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.testing.IncrementingAction
import se.gustavkarlsson.conveyor.testing.SimpleAtomicStateFlow
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope
import se.gustavkarlsson.conveyor.testing.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

object ActionExecutorTest : Spek({
    val initialState = 0
    val scope by memoizedTestCoroutineScope()
    val actions by memoized { MutableSharedFlow<Action<Int>>() }
    val stateFlow by memoized { SimpleAtomicStateFlow(initialState) }

    describe("A minimal ActionExecutor") {
        val subject by memoized {
            ActionExecutor(
                startActions = emptyList(),
                actions = actions,
                transformers = emptyList(),
                stateFlow = stateFlow,
            )
        }

        it("does not execute action before run") {
            runBlockingTest {
                actions.emit(IncrementingAction(1))
            }
            expectThat(stateFlow.value).isEqualTo(0)
        }

        describe("that was run") {
            beforeEachTest {
                scope.launch { subject.run() }
            }

            it("executes action") {
                runBlockingTest {
                    actions.emit(IncrementingAction(1))
                }
                expectThat(stateFlow.value).isEqualTo(1)
            }
            it("executes actions in parallel") {
                runBlockingTest {
                    actions.emit(IncrementingAction(1, 100))
                    actions.emit(IncrementingAction(1, 100))
                    scope.advanceTimeBy(100)
                }
                expectThat(stateFlow.value).isEqualTo(2)
            }
            it("throws if run again") {
                expectThrows<IllegalStateException> {
                    runBlockingTest {
                        subject.run()
                    }
                }
            }
        }
    }
    describe("An ActionExecutor with 2 start actions") {
        val startAction1 = IncrementingAction(1)
        val startAction2 = IncrementingAction(2)
        val subject by memoized {
            ActionExecutor(
                startActions = listOf(startAction1, startAction2),
                actions = actions,
                transformers = emptyList(),
                stateFlow = stateFlow,
            )
        }

        it("doesn't execute start actions before run") {
            expectThat(stateFlow.value).isEqualTo(0)
        }

        describe("that was run") {
            beforeEachTest {
                scope.launch { subject.run() }
            }

            it("executed start actions") {
                expectThat(stateFlow.value).isEqualTo(3)
            }
        }
    }
    describe("An ActionExecutor with 2 transformers that was run") {
        val startAction = IncrementingAction(1)
        val transformer1 = { flow: Flow<Action<Int>> ->
            flow.flatMapConcat { action ->
                flowOf(action, action)
            }
        }
        val transformer2 = { flow: Flow<Action<Int>> ->
            flow.drop(1)
        }
        val subject by memoized {
            ActionExecutor(
                startActions = listOf(startAction),
                actions = actions,
                transformers = listOf(transformer1, transformer2),
                stateFlow = stateFlow,
            )
        }
        beforeEachTest {
            scope.launch { subject.run() }
        }

        it("properly transforms actions") {
            runBlockingTest {
                actions.emit(IncrementingAction(2))
            }
            expectThat(stateFlow.value).isEqualTo(5)
        }
    }
})
