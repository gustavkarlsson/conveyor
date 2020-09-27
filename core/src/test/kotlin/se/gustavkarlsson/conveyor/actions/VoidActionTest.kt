package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.atomic.AtomicInteger

object VoidActionTest : Spek({
    val nullIssuer = object : CommandIssuer<Nothing> {
        override fun issue(command: Command<Nothing>) = Unit
    }
    val blockInvocationCount by memoized { AtomicInteger() }
    val subject = VoidAction<Nothing> { blockInvocationCount.incrementAndGet() }

    describe("An action") {
        it("does not run block automatically") {
            expectThat(blockInvocationCount.get()).isEqualTo(0)
        }
        it("runs block when executed") {
            runBlockingTest {
                subject.execute(nullIssuer)
            }
            expectThat(blockInvocationCount.get()).isEqualTo(1)
        }
        it("runs block twice when executed twice") {
            runBlockingTest {
                subject.execute(nullIssuer)
                subject.execute(nullIssuer)
            }
            expectThat(blockInvocationCount.get()).isEqualTo(2)
        }
    }
})
