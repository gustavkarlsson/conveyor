package se.gustavkarlsson.conveyor.rx2.internal

import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.rx2.test.SimpleStateAccess
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object RxStateAccessImplTest : Spek({
    val stateAccess by memoized { SimpleStateAccess(1) }

    describe("An RxStateAccessImpl") {
        val subject by memoized { RxStateAccessImpl(stateAccess) }

        it("get gets state") {
            val result = subject.get()
            expectThat(result).isEqualTo(1)
        }
        it("set sets state") {
            subject.set(2).blockingAwait()
            val result = stateAccess.state.value
            expectThat(result).isEqualTo(2)
        }
        it("update returns new state") {
            val updateResult = subject.update { map { it + 1 } }.blockingGet()
            expectThat(updateResult).isEqualTo(2)
        }
        it("update sets state") {
            subject.update { map { it + 1 } }.blockingGet()
            val result = subject.get()
            expectThat(result).isEqualTo(2)
        }
        it("flowable gets flow") {
            val testSubscriber = subject.state.test()
            runBlocking { stateAccess.set(2) }
            testSubscriber.assertValues(1, 2)
        }
    }
})
