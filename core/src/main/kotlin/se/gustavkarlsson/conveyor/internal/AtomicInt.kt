package se.gustavkarlsson.conveyor.internal

// TODO Add tests
internal class AtomicInt(initial: Int = 0) {
    private var value = initial

    @Synchronized
    fun incrementAndGet(): Int = ++value

    @Synchronized
    fun decrementAndGet(): Int = --value
}
