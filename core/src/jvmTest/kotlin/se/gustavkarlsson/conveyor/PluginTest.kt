package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object PluginTest : Spek({
    val scope by memoizedTestCoroutineScope()

    describe("A store with overridden initial state") {
        val plugin1 = object : Plugin<Int> {
            override fun overrideInitialState(initialState: Int) =
                initialState + 2
        }
        val plugin2 = object : Plugin<Int> {
            override fun overrideInitialState(initialState: Int) =
                initialState * 2
        }
        val store by memoized {
            Store(1, plugins = listOf(plugin1, plugin2))
        }

        it("has expected initial state") {
            val result = store.state.value
            expectThat(result).isEqualTo(6)
        }
    }
    describe("A store with added start actions") {
        val plugin1 = object : Plugin<Int> {
            override fun addStartActions(): Iterable<Action<Int>> {
                return listOf(Action { it.update { this + 2 } })
            }
        }
        val plugin2 = object : Plugin<Int> {
            override fun addStartActions(): Iterable<Action<Int>> {
                return listOf(Action { it.update { this * 2 } })
            }
        }
        val store by memoized {
            Store(1, plugins = listOf(plugin1, plugin2))
        }

        it("has expected state after starting") {
            store.start(scope)
            val result = store.state.value
            expectThat(result).isEqualTo(6)
        }
    }
    describe("A store with transformed actions") {
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
        val store by memoized {
            Store(1, plugins = listOf(plugin1, plugin2))
        }

        it("has expected state after issuing actions") {
            store.start(scope)
            store.issue {
                it.update { this + 2 }
            }
            store.issue {
                it.update { this * 2 }
            }
            val result = store.state.value
            expectThat(result).isEqualTo(12)
        }
    }
    describe("A store with transformed state") {
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
        val store by memoized {
            Store(1, plugins = listOf(plugin1, plugin2))
        }

        it("has expected state after issuing actions") {
            store.start(scope)
            val initialState = store.state.value
            store.issue {
                it.update { this + 2 }
            }
            val updatedState = store.state.value
            expect {
                that(initialState).isEqualTo(6)
                that(updatedState).isEqualTo(10)
            }
        }
    }
})
