package se.gustavkarlsson.conveyor.internal

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class AtomicIntTest : Spek({
    describe("An AtomicInt") {
        val subject by memoized { AtomicInt(0) }

        it("incrementAndGet returns incremented value") {
            val result = subject.incrementAndGet()
            expectThat(result).isEqualTo(1)
        }
        it("decrementAndGet returns decremented value") {
            val result = subject.decrementAndGet()
            expectThat(result).isEqualTo(-1)
        }
        it("increment and then decrement returns initial value") {
            subject.incrementAndGet()
            val result = subject.decrementAndGet()
            expectThat(result).isEqualTo(0)
        }
    }
})
