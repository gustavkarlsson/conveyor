package se.gustavkarlsson.conveyor

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class PluginTest : FunSpec({

    test("store with overridden initial state has expected initial state") {
        val plugin1 = object : Plugin<Int> {
            override fun overrideInitialState(initialState: Int) = initialState + 2
        }
        val plugin2 = object : Plugin<Int> {
            override fun overrideInitialState(initialState: Int) = initialState * 2
        }
        val store = Store(1, plugins = listOf(plugin1, plugin2))

        val result = store.state.value
        result.shouldBe(6)
    }

    test("store with added start action shas expected state after starting") {
        val plugin1 = object : Plugin<Int> {
            override fun addStartActions(): Iterable<Action<Int>> {
                return listOf(
                    Action { storeFlow ->
                        storeFlow.update { it + 2 }
                    }
                )
            }
        }
        val plugin2 = object : Plugin<Int> {
            override fun addStartActions(): Iterable<Action<Int>> {
                return listOf(
                    Action { storeFlow ->
                        storeFlow.update { it * 2 }
                    }
                )
            }
        }
        val store = Store(1, plugins = listOf(plugin1, plugin2))

        runTest {
            val runJob = launch { store.run() }
            runCurrent()
            val result = store.state.value
            result.shouldBe(6)
            runJob.cancel()
        }
    }

    test("store with transformed actions has expected state after issuing actions") {
        val plugin1 = object : Plugin<Int> {
            override fun transformActions(actions: Flow<Action<Int>>): Flow<Action<Int>> {
                return actions.flatMapConcat { flowOf(it, it) }
            }
        }
        val plugin2 = object : Plugin<Int> {
            override fun transformActions(actions: Flow<Action<Int>>): Flow<Action<Int>> {
                return actions.drop(1)
            }
        }
        val store = Store(1, plugins = listOf(plugin1, plugin2))

        runTest {
            val runJob = launch { store.run() }
            runCurrent()
            store.issue { storeFlow ->
                storeFlow.update { it + 2 }
            }
            store.issue { storeFlow ->
                storeFlow.update { it * 2 }
            }
            runCurrent()
            val result = store.state.value
            result.shouldBe(12)
            runJob.cancel()
        }
    }

    test("store with transformed state has expected state after issuing actions") {
        val plugin1 = object : Plugin<Int> {
            override fun transformStates(states: Flow<Int>): Flow<Int> {
                return states.map { it + 2 }
            }
        }
        val plugin2 = object : Plugin<Int> {
            override fun transformStates(states: Flow<Int>): Flow<Int> {
                return states.map { it * 2 }
            }
        }
        val store = Store(1, plugins = listOf(plugin1, plugin2))

        runTest {
            val runJob = launch { store.run() }
            runCurrent()
            val initialState = store.state.value
            store.issue { storeFlow ->
                storeFlow.update { it + 2 }
            }
            runCurrent()
            val updatedState = store.state.value
            assertSoftly {
                initialState.shouldBe(6)
                updatedState.shouldBe(10)
            }
            runJob.cancel()
        }
    }
})
