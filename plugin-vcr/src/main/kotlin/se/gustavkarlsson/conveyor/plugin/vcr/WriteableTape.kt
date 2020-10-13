package se.gustavkarlsson.conveyor.plugin.vcr

public interface WriteableTape<State> {
    public suspend fun write(state: State)
}
