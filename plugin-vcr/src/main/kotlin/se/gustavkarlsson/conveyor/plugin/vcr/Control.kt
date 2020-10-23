package se.gustavkarlsson.conveyor.plugin.vcr

public interface Control<State> {
    public fun play(tape: ReadableTape<State>)
    public fun record(tape: WriteableTape<State>)
    public fun stop()
}
