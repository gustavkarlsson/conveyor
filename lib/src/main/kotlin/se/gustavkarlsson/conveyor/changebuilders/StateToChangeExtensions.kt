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
public fun <State> State.only(): Change<State> = Change(this)

// TODO Add tests for null values
@ExperimentalChangeBuilder
public fun <State> State.with(
    actions: List<Action<State>?>
): Change<State> = Change(this, actions.filterNotNull())

@ExperimentalChangeBuilder
public fun <State> State.with(
    vararg actions: Action<State>?
): Change<State> = with(actions.asList())

@ExperimentalChangeBuilder
public fun <State> State.with(
    action: Action<State>?
): Change<State> = with(listOf(action))

@ExperimentalChangeBuilder
public fun <State> State.withVoid(
    block: suspend () -> Unit
): Change<State> = with(VoidAction(block))

@ExperimentalChangeBuilder
public fun <State> State.withSingle(
    block: suspend () -> Command<State>
): Change<State> = with(SingleAction(block))

@ExperimentalChangeBuilder
public fun <State> State.withMulti(
    block: suspend CommandIssuer<State>.() -> Unit
): Change<State> = with(MultiAction(block))

@ExperimentalChangeBuilder
public fun <State> State.withFlow(
    flow: Flow<Command<State>>
): Change<State> = with(FlowAction(flow))
