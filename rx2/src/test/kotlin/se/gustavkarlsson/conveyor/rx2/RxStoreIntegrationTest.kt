package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope

object RxStoreIntegrationTest : Spek({
    val scope by memoizedTestCoroutineScope()

    describe("A started RxStore") {
        val subject by memoized { Store(0).asRxStore() }

        var disposable: Disposable? = null
        beforeEachTest { disposable = subject.start(scope) }
        afterEachTest { disposable?.dispose() }

        it("Executes update sequentially") {
            val testSubscriber = subject.state.test()
            subject.issue { state ->
                state.update { 1 }.ignoreElement()
            }
            testSubscriber.assertValues(0, 1)
        }

        it("Executes updateBlocking sequentially") {
            val testSubscriber = subject.state.test()
            subject.issue { state ->
                Completable.fromAction {
                    state.updateBlocking { 1 }
                }
            }
            testSubscriber.assertValues(0, 1)
        }
    }
})
