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

@RequiresOptIn(message = "This API is experimental. To ensure stability, use a Change constructor instead.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public annotation class ExperimentalChangeBuilder

// TODO consider visibility of these extensions.
//  Should they be encapsulated in a DSL?
//  What about state.with(action).with(action) returning the wrong type?
//

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
