package se.gustavkarlsson.conveyor.plugin.vcr.tapes

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

public class SimpleFileTape<T>(
    file: File,
    private val deserialize: (ByteArray) -> T,
    private val serialize: (T) -> ByteArray,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractFileTape<T>(file, bufferSize, dispatcher) {
    override fun readSample(stream: InputStream): Sample<T> =
        stream.readSample(deserialize)

    override fun writeSample(sample: Sample<T>, stream: OutputStream): Unit =
        stream.writeSample(sample, serialize)
}

private enum class SampleType(val id: Int) {
    Delay(1), State(2)
}

private fun <T> InputStream.readSample(deserialize: (ByteArray) -> T): Sample<T> =
    when (readSampleType()) {
        SampleType.Delay -> readDelay()
        SampleType.State -> readState(deserialize)
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

private fun <T> InputStream.readState(deserialize: (ByteArray) -> T): Sample.State<T> {
    val size = readInt()
    val bytes = readNBytes(size)
    val state = deserialize(bytes)
    return Sample.State(state)
}

private fun InputStream.readInt(): Int {
    val bytes = readNBytes(Int.SIZE_BYTES)
    return ByteBuffer.wrap(bytes).int
}

private fun InputStream.readLong(): Long {
    val bytes = readNBytes(Long.SIZE_BYTES)
    return ByteBuffer.wrap(bytes).long
}

private fun <T> OutputStream.writeSample(sample: Sample<T>, serialize: (T) -> ByteArray) {
    when (sample) {
        is Sample.Delay -> writeDelay(sample.timeMillis)
        is Sample.State -> writeState(sample.state, serialize)
    }
}

private fun OutputStream.writeDelay(timeMillis: Long) {
    writeSampleType(SampleType.Delay)
    writeLong(timeMillis)
}

private fun <T> OutputStream.writeState(state: T, serialize: (T) -> ByteArray) {
    writeSampleType(SampleType.State)
    val bytes = serialize(state)
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
