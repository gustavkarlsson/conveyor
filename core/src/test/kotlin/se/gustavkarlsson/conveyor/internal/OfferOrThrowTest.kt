package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.channels.Channel
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThrows

object OfferOrThrowTest : Spek({
    describe("A channel with capacity 1") {
        val channel by memoized { Channel<String>(1) }

        it("accepts one offered element") {
            channel.offerOrThrow("element")
        }

        it("throws when offered two elements") {
            channel.offerOrThrow("shouldAccept")
            expectThrows<IllegalStateException> {
                channel.offerOrThrow("shouldThrow")
            }
        }
    }
})
