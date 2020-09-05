package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Change
import se.gustavkarlsson.conveyor.Command

class FixedStateCommand<State>(private val value: State) : Command<State> {
    override fun reduce(state: State): Change<State> = Change(value)
}
