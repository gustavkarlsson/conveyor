package se.gustavkarlsson.conveyor.plugin.vcr

/**
 * Which mode to run the VCR in, [Record] or [Play]
 */
public sealed interface Mode<out State> {

    /**
     * Record the states into a tape
     */
    public data class Record<State>(
        val tape: WriteableTape<State>,
        val bufferSize: Int = DEFAULT_PLAY_BUFFER_SIZE,
    ) : Mode<State>

    /**
     * Play back a recording from a tape
     */
    public data class Play<State>(
        val tape: ReadableTape<State>,
        val bufferSize: Int = DEFAULT_RECORD_BUFFER_SIZE,
    ) : Mode<State>

    public companion object {
        public const val DEFAULT_PLAY_BUFFER_SIZE: Int = 32
        public const val DEFAULT_RECORD_BUFFER_SIZE: Int = 32
    }
}
