package se.gustavkarlsson.cokrate.test

import se.gustavkarlsson.cokrate.Change
import se.gustavkarlsson.cokrate.Command

class FixedStateCommand<T : Any>(private val value: T) : Command<T> {
    override fun reduce(state: T): Change<T> = Change(value)
}
