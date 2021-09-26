package se.gustavkarlsson.conveyor.plugin.vcr

public data class Sample<out State>(val timestampMillis: Long, val state: State)
