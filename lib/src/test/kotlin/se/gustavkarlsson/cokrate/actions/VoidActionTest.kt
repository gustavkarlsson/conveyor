package se.gustavkarlsson.cokrate.actions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.cokrate.Command
import se.gustavkarlsson.cokrate.CommandIssuer
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
@FlowPreview
object VoidActionTest : Spek({
    val nullIssuer = object : CommandIssuer<Nothing> {
        override suspend fun issue(command: Command<Nothing>) = Unit
    }
    val blockInvocationCount by memoized { AtomicInteger() }
    val action by memoized {
        VoidAction<Nothing> { blockInvocationCount.incrementAndGet() }
    }

    describe("An action") {
        it("does not run block automatically") {
            expectThat(blockInvocationCount.get()).isEqualTo(0)
        }
        it("runs block when executed") {
            runBlocking {
                action.execute(nullIssuer)
            }
            expectThat(blockInvocationCount.get()).isEqualTo(1)
        }
        it("runs block twice when executed twice") {
            runBlocking {
                action.execute(nullIssuer)
                action.execute(nullIssuer)
            }
            expectThat(blockInvocationCount.get()).isEqualTo(2)
        }
    }
})
