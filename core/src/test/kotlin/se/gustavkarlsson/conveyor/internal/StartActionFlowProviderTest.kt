package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.action
import se.gustavkarlsson.conveyor.test.IncrementStateAction
import se.gustavkarlsson.conveyor.test.SimpleStateAccess
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

object StartActionFlowProviderTest : Spek({
    val stateAccess by memoized { SimpleStateAccess(0) }
    val incrementStateAction = IncrementStateAction()

    describe("A provider with one action") {
        val subject by memoized { StartActionProcessor(listOf(incrementStateAction)) }

        it("processing executes action") {
            runBlockingTest {
                subject.process(stateAccess)
            }
            expectThat(stateAccess.state.value).isEqualTo(1)
        }
        it("processing twice throws exception") {
            expectThrows<IllegalStateException> {
                runBlockingTest {
                    subject.process(stateAccess)
                    subject.process(stateAccess)
                }
            }
        }
    }
    describe("A provider with a two delayed actions") {
        val delayAction = action<Int> { access ->
            delay(1)
            access.update { this + 1 }
        }
        val subject by memoized { StartActionProcessor(listOf(delayAction, delayAction)) }

        it("processing executes actions in parallel") {
            runBlockingTest {
                launch { subject.process(stateAccess) }
                advanceTimeBy(1)
                expectThat(stateAccess.state.value).isEqualTo(2)
            }
        }
    }
})
