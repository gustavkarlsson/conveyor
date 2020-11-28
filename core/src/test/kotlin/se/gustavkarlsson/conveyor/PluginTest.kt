package se.gustavkarlsson.conveyor

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
    describe("A store with overridden start actions") {
        val plugin1 = object : Plugin<Int> {
            override fun overrideStartActions(startActions: Iterable<Action<Int>>) =
                startActions + Action { it.update { this + 2 } }
        }
        val plugin2 = object : Plugin<Int> {
            override fun overrideStartActions(startActions: Iterable<Action<Int>>) =
                startActions + Action { it.update { this * 2 } }
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
    describe("A store with overridden action transformers") {
        val plugin1 = object : Plugin<Int> {
            override fun overrideActionTransformers(actionTransformers: Iterable<Transformer<Action<Int>>>) =
                actionTransformers + Transformer { actions ->
                    actions.flatMapConcat { flowOf(it, it) }
                }
        }
        val plugin2 = object : Plugin<Int> {
            override fun overrideActionTransformers(actionTransformers: Iterable<Transformer<Action<Int>>>) =
                actionTransformers + Transformer { actions ->
                    actions.drop(1)
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
    describe("A store with overridden state transformers") {
        val plugin1 = object : Plugin<Int> {
            override fun overrideStateTransformers(stateTransformers: Iterable<Transformer<Int>>) =
                stateTransformers + Transformer { states ->
                    states.map { it + 2 }
                }
        }
        val plugin2 = object : Plugin<Int> {
            override fun overrideStateTransformers(stateTransformers: Iterable<Transformer<Int>>) =
                stateTransformers + Transformer { states ->
                    states.map { it * 2 }
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
