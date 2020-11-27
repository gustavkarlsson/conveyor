package se.gustavkarlsson.conveyor.plugin.vcr.tapes

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

public class SimpleFileTape<State>(
    file: File,
    private val serializer: Serializer<State>,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractFileTape<State>(file, bufferSize, dispatcher) {
    override fun readSample(stream: InputStream): Sample<State> =
        stream.readSample(serializer)

    override fun writeSample(sample: Sample<State>, stream: OutputStream): Unit =
        stream.writeSample(sample, serializer)

    public interface Serializer<State> {
        public fun serialize(state: State): ByteArray
        public fun deserialize(bytes: ByteArray): State
    }
}

private enum class SampleType(val id: Int) {
    Delay(1), State(2)
}

private fun <State> InputStream.readSample(serializer: SimpleFileTape.Serializer<State>): Sample<State> =
    when (readSampleType()) {
        SampleType.Delay -> readDelay()
        SampleType.State -> readState(serializer)
    }

private fun InputStream.readSampleType(): SampleType {
    val typeId = readInt()
    val type = SampleType.values().find { it.id == typeId }
    return type ?: error("Unsupported sample type ID: $typeId")
}

private fun InputStream.readDelay(): Sample.Delay {
    val timeMillis = readLong()
    return Sample.Delay(timeMillis)
}

private fun <State> InputStream.readState(serializer: SimpleFileTape.Serializer<State>): Sample.State<State> {
    val size = readInt()
    val bytes = readNBytes(size)
    val state = serializer.deserialize(bytes)
    return Sample.State(state)
}

private fun InputStream.readLong(): Long {
    val bytes = readNBytes(Long.SIZE_BYTES)
    return ByteBuffer.wrap(bytes).long
}

private fun InputStream.readInt(): Int {
    val bytes = readNBytes(Int.SIZE_BYTES)
    return ByteBuffer.wrap(bytes).int
}

private fun <State> OutputStream.writeSample(sample: Sample<State>, serializer: SimpleFileTape.Serializer<State>) {
    when (sample) {
        is Sample.Delay -> writeDelay(sample.delayMillis)
        is Sample.State -> writeState(sample.state, serializer)
    }
}

private fun OutputStream.writeDelay(timeMillis: Long) {
    writeSampleType(SampleType.Delay)
    writeLong(timeMillis)
}

private fun <State> OutputStream.writeState(state: State, serializer: SimpleFileTape.Serializer<State>) {
    writeSampleType(SampleType.State)
    val bytes = serializer.serialize(state)
    writeInt(bytes.size)
    write(bytes)
}

private fun OutputStream.writeSampleType(type: SampleType) {
    writeInt(type.id)
}

private fun OutputStream.writeInt(value: Int) {
    val bytes = ByteArray(Int.SIZE_BYTES)
    ByteBuffer.wrap(bytes).putInt(value)
    write(bytes)
}

private fun OutputStream.writeLong(value: Long) {
    val bytes = ByteArray(Long.SIZE_BYTES)
    ByteBuffer.wrap(bytes).putLong(value)
    write(bytes)
}
