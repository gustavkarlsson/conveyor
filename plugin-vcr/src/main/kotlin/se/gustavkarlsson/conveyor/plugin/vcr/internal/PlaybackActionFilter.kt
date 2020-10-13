package se.gustavkarlsson.conveyor.plugin.vcr.internal

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Mapper

internal class PlaybackActionFilter<State>(
    private val getMode: () -> Mode<State>
) : Mapper<Action<State>> {
    override suspend fun map(value: Action<State>): Action<State>? =
        if (getMode() is Mode.Playing<State>) {
            null
        } else {
            value
        }
}
