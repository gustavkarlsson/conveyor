package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Change
import se.gustavkarlsson.conveyor.Command

class FixedStateCommand<T : Any>(private val value: T) : Command<T> {
    override fun reduce(state: T): Change<T> = Change(value)
}
