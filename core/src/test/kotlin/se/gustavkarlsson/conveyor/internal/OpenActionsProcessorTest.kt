package se.gustavkarlsson.conveyor.internal

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.test.NullAction
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly

object OpenActionsProcessorTest : Spek({
    val executedActions by memoized { mutableListOf<Action<String>>() }
    val nullAction = NullAction<String>()

    describe("A processor with a single null action") {
        val subject by memoized { OpenActionsProcessor(listOf(nullAction)) }

        it("process executes action") {
            runBlockingTest {
                subject.process {
                    executedActions += it
                }
            }
            expectThat(executedActions).containsExactly(nullAction)
        }

        it("process twice throws exception") {
            expectThrows<IllegalStateException> {
                runBlockingTest {
                    subject.process {}
                    subject.process {}
                }
            }
        }
    }
})
