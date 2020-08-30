package se.gustavkarlsson.cokrate

import kotlin.collections.plus as collectionsPlus

public data class Change<State : Any, Command : Any>(
    val state: State,
    val actions: Iterable<Action<Command>>
)

public fun <State : Any> State.only(): Change<State, Nothing> =
    Change(this, emptyList())

public operator fun <State : Any, Command : Any> State.plus(
    action: Action<Command>
): Change<State, Command> =
    Change(this, listOf(action))

public operator fun <State : Any, Command : Any> State.plus(
    actions: Iterable<Action<Command>>
): Change<State, Command> =
    Change(this, actions)

public operator fun <State : Any, Command : Any> Change<State, Command>.plus(
    actions: Iterable<Action<Command>>
): Change<State, Command> =
    Change(this.state, this.actions.collectionsPlus(actions))

public operator fun <State : Any, Command : Any> Change<State, Command>.plus(
    action: Action<Command>
): Change<State, Command> =
    Change(this.state, this.actions.collectionsPlus(action))
