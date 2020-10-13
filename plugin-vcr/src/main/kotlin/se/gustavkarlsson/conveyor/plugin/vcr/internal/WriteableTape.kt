package se.gustavkarlsson.conveyor.plugin.vcr.internal

internal interface WriteableTape<State> {
    suspend fun write(state: State)
}
