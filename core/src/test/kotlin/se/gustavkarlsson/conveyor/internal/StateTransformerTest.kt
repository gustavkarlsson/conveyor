package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object StateTransformerTest : Spek({
    val initialState = 0
    val scope by memoizedTestCoroutineScope()
    val incomingState by memoized { MutableStateFlow(initialState) }

    describe("A StateTransformer with no transformers") {
        val subject by memoized { StateTransformer(incomingState, emptyList()) }
        beforeEachTest { subject.outgoingState } // Necessary to create instance before its first accessed

        it("value is initial value") {
            expectThat(subject.outgoingState.value).isEqualTo(0)
        }
        it("state doesn't change when setting incoming state") {
            incomingState.value = 1
            expectThat(subject.outgoingState.value).isEqualTo(0)
        }

        describe("that was launched") {
            beforeEachTest { subject.launch(scope) }

            it("value is initial value") {
                expectThat(subject.outgoingState.value).isEqualTo(0)
            }
            it("state changes when setting incoming state") {
                incomingState.value = 1
                expectThat(subject.outgoingState.value).isEqualTo(1)
            }
        }
    }

    describe("A StateTransformer with 2 transformers that was launched") {
        val doubler = { flow: Flow<Int> ->
            flow.map { it * 2 }
        }
        val adder = { flow: Flow<Int> ->
            flow.map { it + 1 }
        }
        val subject by memoized { StateTransformer(incomingState, listOf(doubler, adder)) }
        beforeEachTest { subject.launch(scope) }

        it("properly transforms state") {
            incomingState.value = 5
            expectThat(subject.outgoingState.value).isEqualTo(11)
        }
    }
})
