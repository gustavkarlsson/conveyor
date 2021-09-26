package se.gustavkarlsson.conveyor.plugin.vcr.tapes

import kotlinx.coroutines.CoroutineDispatcher
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.file.Path

public class SimpleFileTape<State>(
    file: Path,
    private val serializer: Serializer<State>,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    dispatcher: CoroutineDispatcher = createSingleThreadDispatcher(),
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

private fun <State> InputStream.readSample(serializer: SimpleFileTape.Serializer<State>): Sample<State> {
    val timestamp = readLong()
    val state = readState(serializer)
    return Sample(timestamp, state)
}

private fun <State> InputStream.readState(serializer: SimpleFileTape.Serializer<State>): State {
    val size = readInt()
    val bytes = requireNBytes(size)
    return serializer.deserialize(bytes)
}

private fun InputStream.readLong(): Long {
    val bytes = requireNBytes(Long.SIZE_BYTES)
    return ByteBuffer.wrap(bytes).long
}

private fun InputStream.readInt(): Int {
    val bytes = requireNBytes(Int.SIZE_BYTES)
    return ByteBuffer.wrap(bytes).int
}

private fun InputStream.requireNBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    val readCount = read(bytes)
    if (readCount < size) {
        throw IOException("Failed to read $size bytes. Could only read $readCount bytes.")
    }
    return bytes
}

private fun <State> OutputStream.writeSample(sample: Sample<State>, serializer: SimpleFileTape.Serializer<State>) {
    writeLong(sample.timestampMillis)
    writeState(sample.state, serializer)
}

private fun <State> OutputStream.writeState(state: State, serializer: SimpleFileTape.Serializer<State>) {
    val bytes = serializer.serialize(state)
    writeInt(bytes.size)
    write(bytes)
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
