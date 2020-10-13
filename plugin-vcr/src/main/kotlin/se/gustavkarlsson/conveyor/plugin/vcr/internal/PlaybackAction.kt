package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.*
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.plugin.vcr.Mode

internal class PlaybackAction<State>(
    private val mode: Flow<Mode<State>>,
) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        mode
            .filterIsInstance<Mode.Playing<State>>()
            .flatMapLatest { it.tape.read() }
            .collect { stateAccess.set(it) }
    }
}
