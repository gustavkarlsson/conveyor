package se.gustavkarlsson.conveyor.plugin.vcr

public interface Vcr<State> {
    public fun play(tape: ReadableTape<State>, bufferSize: Int = DEFAULT_PLAY_BUFFER_SIZE)
    public fun record(tape: WriteableTape<State>, bufferSize: Int = DEFAULT_RECORD_BUFFER_SIZE)
    public fun stop()

    public companion object {
        public const val DEFAULT_PLAY_BUFFER_SIZE: Int = 8
        public const val DEFAULT_RECORD_BUFFER_SIZE: Int = 8
    }
}
