package se.gustavkarlsson.conveyor.internal

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class ConsumableTest : Spek({
    val value = "value"

    describe("A consumable") {
        val subject by memoized { Consumable(value) }

        it("consuming returns value") {
            val result = subject.consume()
            expectThat(result).isEqualTo(value)
        }
        it("consuming a second time throws") {
            subject.consume()
            expectThrows<IllegalStateException> {
                subject.consume()
            }
        }
    }
})
