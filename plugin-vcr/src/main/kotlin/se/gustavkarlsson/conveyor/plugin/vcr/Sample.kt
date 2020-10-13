package se.gustavkarlsson.conveyor.plugin.vcr

public sealed class Sample<out State> {
    public data class Delay(val timeMillis: Long) : Sample<Nothing>()
    public data class State<State>(val state: State) : Sample<State>()
}
