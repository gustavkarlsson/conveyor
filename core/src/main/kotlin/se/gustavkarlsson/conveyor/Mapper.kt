package se.gustavkarlsson.conveyor

public fun interface Mapper<T> {
    public fun map(value: T): T?
}
