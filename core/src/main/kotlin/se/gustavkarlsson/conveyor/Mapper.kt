package se.gustavkarlsson.conveyor

public interface Mapper<T> {
    public suspend fun map(value: T): T?
}
