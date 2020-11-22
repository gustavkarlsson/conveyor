package se.gustavkarlsson.conveyor.rx2.internal

import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.rx2.testing.SimpleUpdatableStateFlow
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object StateFlowableImplTest : Spek({
    val state by memoized { SimpleUpdatableStateFlow(1) }

    describe("An StateFlowableImpl") {
        val subject by memoized { StateFlowableImpl(state) }

        it("value gets state") {
            val result = subject.value
            expectThat(result).isEqualTo(1)
        }
        it("state gets flow") {
            val testSubscriber = subject.test()
            runBlocking { state.update { 2 } }
            testSubscriber.assertValues(1, 2)
        }
    }
})
