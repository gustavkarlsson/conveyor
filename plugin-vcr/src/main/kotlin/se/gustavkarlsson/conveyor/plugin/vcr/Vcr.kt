package se.gustavkarlsson.conveyor.plugin.vcr

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Mapper
import se.gustavkarlsson.conveyor.Plugin
import se.gustavkarlsson.conveyor.plugin.vcr.internal.*
import kotlin.properties.Delegates.notNull

public class Vcr<State> : Plugin<State>, Control<State> {

    private val modeChannel = ConflatedBroadcastChannel<Mode<State>>(Mode.Idle)

    private val mode = modeChannel.asFlow()

    override fun overrideStateMappers(
        stateMappers: Iterable<Mapper<State>>
    ): Iterable<Mapper<State>> = stateMappers + Recorder(modeChannel::value)

    override fun overrideOpenActions(
        openActions: Iterable<Action<State>>
    ): Iterable<Action<State>> = openActions + PlaybackAction(mode)

    override fun overrideActionMappers(
        actionMappers: Iterable<Mapper<Action<State>>>
    ): Iterable<Mapper<Action<State>>> = actionMappers + PlaybackActionFilter(modeChannel::value)

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
