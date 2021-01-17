package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.testing.IncrementingAction
import se.gustavkarlsson.conveyor.testing.SimpleUpdatableStateFlow
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope
import se.gustavkarlsson.conveyor.testing.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

object ActionExecutorTest : Spek({
    val initialState = 0
    val scope by memoizedTestCoroutineScope()
    val actions by memoized { MutableSharedFlow<Action<Int>>() }
    val state by memoized { SimpleUpdatableStateFlow(initialState) }

    describe("A minimal ActionExecutor") {
        val subject by memoized {
            ActionExecutor(
                startActions = emptyList(),
                actions = actions,
                transformers = emptyList(),
                state = state,
            )
        }

        it("does not execute action before launch") {
            runBlockingTest {
                actions.emit(IncrementingAction(1))
            }
            expectThat(state.value).isEqualTo(0)
        }

        describe("that was launched") {
            beforeEachTest { subject.launch(scope) }

            it("executes action") {
                runBlockingTest {
                    actions.emit(IncrementingAction(1))
                }
                expectThat(state.value).isEqualTo(1)
            }
            it("executes actions in parallel") {
                runBlockingTest {
                    actions.emit(IncrementingAction(1, 100))
                    actions.emit(IncrementingAction(1, 100))
                    scope.advanceTimeBy(100)
                }
                expectThat(state.value).isEqualTo(2)
            }
            it("throws if launched again") {
                expectThrows<IllegalStateException> {
                    runBlockingTest {
                        subject.launch(this)
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
                state = state,
            )
        }

        it("doesn't execute start actions before launch") {
            expectThat(state.value).isEqualTo(0)
        }

        describe("that was launched") {
            beforeEachTest { subject.launch(scope) }

            it("executed start actions") {
                expectThat(state.value).isEqualTo(3)
            }
        }
    }
    describe("An ActionExecutor with 2 transformers that was launched") {
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
                state = state,
            )
        }
        beforeEachTest { subject.launch(scope) }

        it("properly transforms actions") {
            runBlockingTest {
                actions.emit(IncrementingAction(2))
            }
            expectThat(state.value).isEqualTo(5)
        }
    }
})
