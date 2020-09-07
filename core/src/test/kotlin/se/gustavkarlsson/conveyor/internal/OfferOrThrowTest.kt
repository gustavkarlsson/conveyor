package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.actions.VoidAction
import se.gustavkarlsson.conveyor.test.NullAction
import se.gustavkarlsson.conveyor.test.TrackingCommandIssuer
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.util.concurrent.atomic.AtomicInteger

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
