package se.gustavkarlsson.conveyor

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.gustavkarlsson.conveyor.testing.NullAction
import se.gustavkarlsson.conveyor.testing.SetStateAction

class StoreIntegrationTest : FunSpec({
    val initialState = "initial"
    val state1 = "state1"
    val fixedStateAction1 = SetStateAction(state1)
    val action = NullAction<String>()

    val minimalStore = Store(initialState)

    val failingAction = Action<String> { error("failed") }
    val storeWithFailingAction = Store(initialState, startActions = listOf(failingAction))

    val failingWhileUpdatingAction = Action<String> { storeFlow -> storeFlow.update { error("failed") } }
    val storeWithFailingWhileUpdatingAction = Store(initialState, startActions = listOf(failingWhileUpdatingAction))

    test("state emits initial") {
        runTest {
            val result = minimalStore.state.first()
            runCurrent()
            result.shouldBe(initialState)
        }
    }

    test("state.value returns initial") {
        val result = minimalStore.state.value
        result.shouldBe(initialState)
    }

    test("throws when issuing action") {
        shouldThrow<StoreNotYetStartedException> {
            minimalStore.issue(fixedStateAction1)
        }
    }

    test("A store with a starting action that fails when updating cancels when started") {
        shouldThrow<IllegalStateException> {
            storeWithFailingWhileUpdatingAction.run()
        }
    }

    test("A store with a starting action that fails when executed cancels when started") {
        shouldThrow<IllegalStateException> {
            storeWithFailingAction.run()
        }
    }

    test("started store has an active job") {
        runTest {
            val runJob = launch { minimalStore.run() }
            runJob.isActive.shouldBeTrue()
            runJob.cancel()
        }
    }

    test("started store throws exception when started") {
        runTest {
            val runJob = launch { minimalStore.run() }
            runCurrent()
            shouldThrow<StoreAlreadyStartedException> {
                minimalStore.run()
            }
            runJob.cancel()
        }
    }

    test("started store state emits initial") {
        runTest {
            val runJob = launch { minimalStore.run() }
            runCurrent()
            val result = minimalStore.state.first()
            result.shouldBe(initialState)
            runJob.cancel()
        }
    }

    test("a store that was started and then cancelled throws exception when started") {
        runTest {
            val runJob = launch { minimalStore.run() }
            runCurrent()
            runJob.cancel()
            runCurrent()
            shouldThrow<StoreStoppedException> {
                minimalStore.run()
            }
        }
    }
    test("a store that was started and then cancelled throws exception when an action is issued") {
        runTest {
            val runJob = launch { minimalStore.run() }
            runCurrent()
            runJob.cancel()
            runCurrent()
            shouldThrow<StoreStoppedException> {
                minimalStore.issue(action)
            }
        }
    }

    test("a store that was started and then cancelled, state.value returns initial") {
        runTest {
            val runJob = launch { minimalStore.run() }
            runJob.cancel()
            runCurrent()
            val result = minimalStore.state.value
            result.shouldBe(initialState)
        }
    }

    test("a store that was started and then cancelled, state emits initial") {
        runTest {
            val runJob = launch { minimalStore.run() }
            runCurrent()
            runJob.cancel()
            runCurrent()
            val result = minimalStore.state.first()
            result.shouldBe(initialState)
        }
    }
})
