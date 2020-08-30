package se.gustavkarlsson.cokrate

import kotlin.collections.plus as collectionsPlus

public data class Change<State : Any>(
    val newState: State,
    val actions: Iterable<Action<State>> = emptyList()
)

public fun <State : Any> State.only(): Change<State> =
    Change(this)

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
    copy(actions = actions.collectionsPlus(action))

public operator fun <State : Any> Change<State>.plus(
    actions: Iterable<Action<State>>
): Change<State> =
    copy(actions = this.actions.collectionsPlus(actions))
