package se.gustavkarlsson.conveyor.plugin.vcr

public interface DeletableTape<T> {
    public suspend fun delete(): Boolean
}
