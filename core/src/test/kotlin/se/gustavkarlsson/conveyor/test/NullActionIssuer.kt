package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.ActionIssuer

class NullActionIssuer<State> : ActionIssuer<State> {
    override fun issue(action: Action<State>) = Unit
}
