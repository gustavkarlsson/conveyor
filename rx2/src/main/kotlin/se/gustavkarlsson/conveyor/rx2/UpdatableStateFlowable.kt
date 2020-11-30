package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Single

/**
 * A [StateFlowable] that can be updated sequentially, guaranteeing predictable state changes.
 */
public abstract class UpdatableStateFlowable<State : Any> : StateFlowable<State>() {
    /**
     * Updates the state using the single produced by the given block, returning the new state.
     * The receiver argument of the block is the current state at the time the block runs.
     *
     * State updates run sequentially, which guarantees that the state does not change while an update block runs.
     */
    public abstract fun update(block: State.() -> Single<State>): Single<State>

    /**
     * Updates the state using the given block, returning the new state.
     * The receiver argument of the block is the current state at the time the block runs.
     *
     * State updates run sequentially, which guarantees that the state does not change while an update block runs.
     * This function will therefore block if another update is in progress,
     * even if the provided block does not block.
     */
    public abstract fun updateBlocking(block: State.() -> State): State

    // FIXME verify internal/external subscription count
    /**
     * The number of subscribers of this flow.
     */
    public abstract val subscriptionCount: StateFlowable<Int>
}
