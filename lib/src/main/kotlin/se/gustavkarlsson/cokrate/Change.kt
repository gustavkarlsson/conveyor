package se.gustavkarlsson.cokrate

import kotlin.collections.plus as collectionsPlus

public data class Change<State : Any>(
    val state: State,
    val actions: Iterable<Action<State>>
)

public fun <State : Any> State.only(): Change<State> =
    Change(this, emptyList())

public operator fun <State : Any> State.plus(
    action: Action<State>
): Change<State> =
    Change(this, listOf(action))

public operator fun <State : Any> State.plus(
    actions: Iterable<Action<State>>
): Change<State> =
    Change(this, actions)

public operator fun <State : Any> Change<State>.plus(
    action: Action<State>
): Change<State> =
    Change(this.state, this.actions.collectionsPlus(action))

public operator fun <State : Any> Change<State>.plus(
    actions: Iterable<Action<State>>
): Change<State> =
    Change(this.state, this.actions.collectionsPlus(actions))
