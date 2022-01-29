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
import se.gustavkarlsson.conveyor.testing.SimpleStoreFlow
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope
import se.gustavkarlsson.conveyor.testing.runTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

object ActionExecutorTest : Spek({
    val initialState = 0
    val scope by memoizedTestCoroutineScope()
    val actions by memoized { MutableSharedFlow<Action<Int>>() }
    val storeFlow by memoized { SimpleStoreFlow(initialState) }

    describe("A minimal ActionExecutor") {
        val subject by memoized {
            ActionExecutor(
                startActions = emptyList(),
                actions = actions,
                transformers = emptyList(),
                storeFlow = storeFlow,
            )
        }

        it("does not execute action before run") {
            runTest {
                actions.emit(IncrementingAction(1))
            }
            expectThat(storeFlow.value).isEqualTo(0)
        }

        describe("that was run") {
            beforeEachTest {
                scope.launch { subject.run() }
            }

            it("executes action") {
                runTest {
                    actions.emit(IncrementingAction(1))
                }
                expectThat(storeFlow.value).isEqualTo(1)
            }
            it("executes actions in parallel") {
                runTest {
                    actions.emit(IncrementingAction(1, 100))
                    actions.emit(IncrementingAction(1, 100))
                    scope.testScheduler.advanceTimeBy(100)
                    scope.testScheduler.runCurrent()
                }
                expectThat(storeFlow.value).isEqualTo(2)
            }
            it("throws if run again") {
                expectThrows<IllegalStateException> {
                    runTest {
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
                storeFlow = storeFlow,
            )
        }

        it("doesn't execute start actions before run") {
            expectThat(storeFlow.value).isEqualTo(0)
        }

        describe("that was run") {
            beforeEachTest {
                scope.launch { subject.run() }
            }

            it("executed start actions") {
                expectThat(storeFlow.value).isEqualTo(3)
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
                storeFlow = storeFlow,
            )
        }
        beforeEachTest {
            scope.launch { subject.run() }
        }

        it("properly transforms actions") {
            runTest {
                actions.emit(IncrementingAction(2))
            }
            expectThat(storeFlow.value).isEqualTo(5)
        }
    }
})
