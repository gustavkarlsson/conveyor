package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.rx2.test.SimpleStateManager
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object UpdatableStateFlowableImplTest : Spek({
    val state by memoized { SimpleStateManager(1) }

    describe("An UpdatableStateFlowableImpl") {
        val subject by memoized { UpdatableStateFlowableImpl(state) }

        it("value gets state") {
            val result = subject.value
            expectThat(result).isEqualTo(1)
        }
        it("update returns new state") {
            val updateResult = subject.update { Single.just(this + 1) }.blockingGet()
            expectThat(updateResult).isEqualTo(2)
        }
        it("update sets state") {
            subject.update { Single.just(this + 1) }.blockingGet()
            val result = subject.value
            expectThat(result).isEqualTo(2)
        }
        it("state gets flow") {
            val testSubscriber = subject.test()
            runBlocking { state.update { 2 } }
            testSubscriber.assertValues(1, 2)
        }
    }
})
