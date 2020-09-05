package se.gustavkarlsson.conveyor.changebuilders

import kotlinx.coroutines.flow.Flow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Change
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer
import se.gustavkarlsson.conveyor.actions.FlowAction
import se.gustavkarlsson.conveyor.actions.MultiAction
import se.gustavkarlsson.conveyor.actions.SingleAction
import se.gustavkarlsson.conveyor.actions.VoidAction

@ExperimentalChangeBuilder
public fun <State> Change<State>.and(
    actions: List<Action<State>?>
): Change<State> = copy(actions = (this.actions + actions).filterNotNull())

@ExperimentalChangeBuilder
public fun <State> Change<State>.and(
    vararg actions: Action<State>?
): Change<State> = and(actions.asList())

@ExperimentalChangeBuilder
public fun <State> Change<State>.and(
    action: Action<State>?
): Change<State> = and(listOf(action))

@ExperimentalChangeBuilder
public fun <State> Change<State>.andVoid(
    block: suspend () -> Unit
): Change<State> = and(VoidAction(block))

@ExperimentalChangeBuilder
public fun <State> Change<State>.andSingle(
    block: suspend () -> Command<State>
): Change<State> = and(SingleAction(block))

@ExperimentalChangeBuilder
public fun <State> Change<State>.andMulti(
    block: suspend CommandIssuer<State>.() -> Unit
): Change<State> = and(MultiAction(block))

@ExperimentalChangeBuilder
public fun <State> Change<State>.andFlow(
    flow: Flow<Command<State>>
): Change<State> = and(FlowAction(flow))
