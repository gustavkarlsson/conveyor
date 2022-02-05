package se.gustavkarlsson.conveyor.internal

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.testing.IncrementingAction
import se.gustavkarlsson.conveyor.testing.SimpleStoreFlow

class ActionExecutorTest : FunSpec({
    val initialState = 0
    val actions = MutableSharedFlow<Action<Int>>()
    val minimalStoreFlow = SimpleStoreFlow(initialState)
    val minimalSubject = ActionExecutor(
        startActions = emptyList(),
        actions = actions,
        transformers = emptyList(),
        storeFlow = minimalStoreFlow,
    )

    val twoActionStoreFlow = SimpleStoreFlow(initialState)
    val startAction1 = IncrementingAction(1)
    val startAction2 = IncrementingAction(2)
    val twoActionSubject = ActionExecutor(
        startActions = listOf(startAction1, startAction2),
        actions = actions,
        transformers = emptyList(),
        storeFlow = twoActionStoreFlow,
    )

    val transformerStoreFlow = SimpleStoreFlow(initialState)
    val transformer1 = { flow: Flow<Action<Int>> ->
        flow.flatMapConcat { action ->
            flowOf(action, action)
        }
    }
    val transformer2 = { flow: Flow<Action<Int>> ->
        flow.drop(1)
    }
    val transformerSubject = ActionExecutor(
        startActions = listOf(startAction1),
        actions = actions,
        transformers = listOf(transformer1, transformer2),
        storeFlow = transformerStoreFlow,
    )

    test("does not execute action before run") {
        runTest {
            actions.emit(IncrementingAction(1))
        }
        minimalStoreFlow.value.shouldBe(0)
    }

    test("running executes emitted action") {
        runTest {
            val runJob = launch { minimalSubject.run() }
            runCurrent()
            actions.emit(IncrementingAction(1))
            runCurrent()
            minimalStoreFlow.value.shouldBe(1)
            runJob.cancel()
        }
    }

    test("running executes actions in parallel") {
        runTest {
            val runJob = launch { minimalSubject.run() }
            runCurrent()
            actions.emit(IncrementingAction(1, 100))
            actions.emit(IncrementingAction(1, 100))
            advanceTimeBy(100)
            runCurrent()
            minimalStoreFlow.value.shouldBe(2)
            runJob.cancel()
        }
    }

    test("throws if run a second time") {
        shouldThrow<IllegalStateException> {
            runTest {
                launch { minimalSubject.run() }
                minimalSubject.run()
            }
        }
    }

    test("doesn't execute start actions before run") {
        twoActionStoreFlow.value.shouldBe(0)
    }

    test("executes start actions when run") {
        runTest {
            val runJob = launch { twoActionSubject.run() }
            runCurrent()
            twoActionStoreFlow.value.shouldBe(3)
            runJob.cancel()
        }
    }

    test("properly transforms actions") {
        runTest {
            val runJob = launch { transformerSubject.run() }
            runCurrent()
            actions.emit(IncrementingAction(2))
            runCurrent()
            transformerStoreFlow.value.shouldBe(5)
            runJob.cancel()
        }
    }
})
