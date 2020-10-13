package se.gustavkarlsson.conveyor.plugin.vcr

public interface Control<State> {
    public fun play(tape: ReadableTape<Sample<State>>)
    public fun record(tape: WriteableTape<Sample<State>>)
    public fun stop()
}
