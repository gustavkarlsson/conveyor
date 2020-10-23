package se.gustavkarlsson.conveyor.plugin.vcr

public interface WriteableTape<T> {
    public suspend fun write(sample: Sample<T>)
}
