package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.Flow
import se.gustavkarlsson.conveyor.actions.FlowAction
import se.gustavkarlsson.conveyor.actions.MultiAction
import se.gustavkarlsson.conveyor.actions.SingleAction
import se.gustavkarlsson.conveyor.actions.VoidAction

public data class Change<State>(
    val newState: State,
    val actions: List<Action<State>> = emptyList()
)

// TODO consider visibility of these extensions.
//  Also consider state.with(action).with(action) returning the wrong type

public fun <State> State.only(): Change<State> = Change(this)

// TODO Add tests for null values
public fun <State> State.with(
    actions: List<Action<State>?>
): Change<State> = Change(this, actions.filterNotNull())

public fun <State> State.with(
    vararg actions: Action<State>?
): Change<State> = with(actions.toList())

public fun <State> State.with(
    action: Action<State>?
): Change<State> = with(listOf(action))

public fun <State> State.withVoid(
    block: suspend () -> Unit
): Change<State> = with(VoidAction(block))

public fun <State> State.withSingle(
    block: suspend () -> Command<State>
): Change<State> = with(SingleAction(block))

public fun <State> State.withMulti(
    block: suspend CommandIssuer<State>.() -> Unit
): Change<State> = with(MultiAction(block))

public fun <State> State.withFlow(
    flow: Flow<Command<State>>
): Change<State> = with(FlowAction(flow))

public fun <State> Change<State>.and(
    actions: List<Action<State>?>
): Change<State> = copy(actions = (this.actions + actions).filterNotNull())

public fun <State> Change<State>.and(
    vararg actions: Action<State>?
): Change<State> = and(actions.toList())

public fun <State> Change<State>.and(
    action: Action<State>?
): Change<State> = and(listOf(action))

public fun <State> Change<State>.andVoid(
    block: suspend () -> Unit
): Change<State> = and(VoidAction(block))

public fun <State> Change<State>.andSingle(
    block: suspend () -> Command<State>
): Change<State> = and(SingleAction(block))

public fun <State> Change<State>.andMulti(
    block: suspend CommandIssuer<State>.() -> Unit
): Change<State> = and(MultiAction(block))

public fun <State> Change<State>.andFlow(
    flow: Flow<Command<State>>
): Change<State> = and(FlowAction(flow))
