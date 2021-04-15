package se.gustavkarlsson.conveyor.rx2.internal

import kotlinx.coroutines.Job
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

object JobDisposableTest : Spek({
    val job by memoized { Job() }

    describe("A JobDisposable") {
        val subject by memoized { JobDisposable(job) }

        it("is not disposed") {
            expectThat(subject.isDisposed)
                .describedAs("is disposed")
                .isFalse()
        }

        it("dispose cancels job") {
            subject.dispose()
            expectThat(job.isCancelled)
                .describedAs("job is cancelled")
                .isTrue()
        }

        describe("that was disposed") {
            beforeEachTest { subject.dispose() }

            it("is disposed") {
                expectThat(subject.isDisposed)
                    .describedAs("is disposed")
                    .isTrue()
            }

            it("can be disposed again") {
                subject.dispose()
            }
        }
    }
})
