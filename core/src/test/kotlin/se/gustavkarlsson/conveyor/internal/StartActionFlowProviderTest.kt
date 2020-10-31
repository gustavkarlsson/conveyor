package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toCollection
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.test.NullAction
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly

object StartActionFlowProviderTest : Spek({
    val collectedActions by memoized { mutableListOf<Action<String>>() }
    val nullAction = NullAction<String>()

    describe("A provider with a single null action") {
        val subject by memoized { StartActionFlowProvider(listOf(nullAction)) }

        it("collecting actionFlow executes action") {
            runBlockingTest {
                subject.actionFlow.toCollection(collectedActions)
            }
            expectThat(collectedActions).containsExactly(nullAction)
        }

        it("collecting actionFlow twice throws exception") {
            expectThrows<IllegalStateException> {
                runBlockingTest {
                    subject.actionFlow.collect {}
                    subject.actionFlow.collect {}
                }
            }
        }
    }
})
