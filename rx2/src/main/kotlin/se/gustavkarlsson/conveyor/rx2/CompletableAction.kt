package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.rx2.internal.UpdatableStateFlowableImpl

@ExperimentalCoroutinesApi
public abstract class CompletableAction<State : Any> : Action<State> {
    final override suspend fun execute(state: UpdatableStateFlow<State>) {
        val flowable = UpdatableStateFlowableImpl(state)
        val completable = execute(flowable)
        completable.await()
    }

    protected abstract fun execute(state: UpdatableStateFlowable<State>): Completable
}

@ExperimentalCoroutinesApi
@Suppress("FunctionName")
public fun <State : Any> CompletableAction(
    block: (state: UpdatableStateFlowable<State>) -> Completable,
): CompletableAction<State> = ConstructorCompletableAction(block)

@ExperimentalCoroutinesApi
private class ConstructorCompletableAction<State : Any>(
    private val block: (UpdatableStateFlowable<State>) -> Completable,
) : CompletableAction<State>() {
    override fun execute(state: UpdatableStateFlowable<State>): Completable = block(state)
}
