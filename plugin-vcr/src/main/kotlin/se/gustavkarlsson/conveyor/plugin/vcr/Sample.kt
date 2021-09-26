package se.gustavkarlsson.conveyor.plugin.vcr

public sealed interface Sample<out State> {
    public data class Delay(val delayMillis: Long) : Sample<Nothing>
    public data class State<State>(val state: State) : Sample<State>
}
