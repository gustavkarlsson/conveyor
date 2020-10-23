package se.gustavkarlsson.conveyor.plugin.vcr

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Plugin
import se.gustavkarlsson.conveyor.Transformer
import se.gustavkarlsson.conveyor.plugin.vcr.internal.Mode
import se.gustavkarlsson.conveyor.plugin.vcr.internal.PlaybackAction
import se.gustavkarlsson.conveyor.plugin.vcr.internal.PlaybackActionFilter
import se.gustavkarlsson.conveyor.plugin.vcr.internal.RecordAction
import kotlin.properties.Delegates.notNull

public class Vcr<State> : Plugin<State>, Control<State> {

    private val modeChannel = ConflatedBroadcastChannel<Mode<State>>(Mode.Idle)

    private val mode = modeChannel.asFlow()

    override fun overrideOpenActions(
        openActions: Iterable<Action<State>>
    ): Iterable<Action<State>> = openActions + RecordAction(mode) + PlaybackAction(mode)

    override fun overrideActionTransformers(
        actionTransformers: Iterable<Transformer<Action<State>>>
    ): Iterable<Transformer<Action<State>>> = actionTransformers + PlaybackActionFilter(mode)

    override fun play(tape: ReadableTape<Sample<State>>) {
        val newFlow = tape.read()
            .mapNotNull { sample ->
                when (sample) {
                    is Sample.Delay -> {
                        delay(sample.timeMillis)
                        null
                    }
                    is Sample.State -> sample.state
                }
            }
        val newTape = object : ReadableTape<State> {
            override fun read(): Flow<State> = newFlow
        }
        modeChannel.offer(Mode.Playing(newTape))
    }

    override fun record(tape: WriteableTape<Sample<State>>) {
        val trackPosition = TrackPosition(System::currentTimeMillis)
        trackPosition.start()
        modeChannel.offer(Mode.Recording(tape, trackPosition))
    }

    override fun stop() {
        modeChannel.offer(Mode.Idle)
    }
}

internal class TrackPosition(
    private val getCurrent: () -> Long
) {
    private var last by notNull<Long>()

    fun start() {
        last = getCurrent()
    }

    @Synchronized
    fun getDelta(): Long {
        val current = getCurrent()
        val delta = current - last
        last = current
        return delta
    }
}
