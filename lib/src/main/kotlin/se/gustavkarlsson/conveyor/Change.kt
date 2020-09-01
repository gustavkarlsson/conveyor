package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.Flow
import se.gustavkarlsson.conveyor.actions.FlowAction
import se.gustavkarlsson.conveyor.actions.MultiAction
import se.gustavkarlsson.conveyor.actions.SingleAction
import se.gustavkarlsson.conveyor.actions.VoidAction

public data class Change<State : Any>(
    val newState: State,
    val actions: List<Action<State>> = emptyList()
)

// TODO consider visibility of these extensions.
//  Also consider state.with(action).with(action) returning the wrong type

public fun <State : Any> State.only(): Change<State> = Change(this)

public fun <State : Any> State.with(
    actions: List<Action<State>>
): Change<State> = Change(this, actions)

public fun <State : Any> State.with(
    vararg actions: Action<State>
): Change<State> = with(actions.toList())

public fun <State : Any> State.with(
    action: Action<State>
): Change<State> = with(listOf(action))

public fun <State : Any> State.withVoid(
    block: suspend () -> Unit
): Change<State> = with(VoidAction(block))

public fun <State : Any> State.withSingle(
    block: suspend () -> Command<State>
): Change<State> = with(SingleAction(block))

public fun <State : Any> State.withMulti(
    block: suspend CommandIssuer<State>.() -> Unit
): Change<State> = with(MultiAction(block))

public fun <State : Any> State.withFlow(
    flow: Flow<Command<State>>
): Change<State> = with(FlowAction(flow))

public fun <State : Any> Change<State>.and(
    actions: List<Action<State>>
): Change<State> = copy(actions = this.actions + actions)

public fun <State : Any> Change<State>.and(
    vararg actions: Action<State>
): Change<State> = and(actions.toList())

public fun <State : Any> Change<State>.and(
    action: Action<State>
): Change<State> = and(listOf(action))

public fun <State : Any> Change<State>.andVoid(
    block: suspend () -> Unit
): Change<State> = and(VoidAction(block))

public fun <State : Any> Change<State>.andSingle(
    block: suspend () -> Command<State>
): Change<State> = and(SingleAction(block))

public fun <State : Any> Change<State>.andMulti(
    block: suspend CommandIssuer<State>.() -> Unit
): Change<State> = and(MultiAction(block))

public fun <State : Any> Change<State>.andFlow(
    flow: Flow<Command<State>>
): Change<State> = and(FlowAction(flow))
