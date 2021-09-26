package se.gustavkarlsson.conveyor.plugin.vcr.tapes

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import se.gustavkarlsson.conveyor.plugin.vcr.WriteableTape
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

// TODO Migrate to some KMP IO solution
public abstract class AbstractFileTape<T>(
    private val file: Path,
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE,
    private val dispatcher: CoroutineDispatcher = createSingleThreadDispatcher(),
) : ReadableTape<T>, WriteableTape<T> {
    final override fun openForReading(): ReadableTape.Reading<T> = Reading()

    final override fun openForWriting(): WriteableTape.Writing<T> = Writing()

    private inner class Reading : ReadableTape.Reading<T> {
        private val stream = file.inputStream()

        override suspend fun read(): Sample<T>? =
            withContext(dispatcher) {
                @Suppress("BlockingMethodInNonBlockingContext")
                if (stream.available() > 0) {
                    readSample(stream)
                } else null
            }

        override fun close() = stream.close()
    }

    private inner class Writing : WriteableTape.Writing<T> {
        private val stream = file.outputStream().buffered(bufferSize)

        override suspend fun write(sample: Sample<T>) =
            withContext(dispatcher) {
                writeSample(sample, stream)
            }

        override fun close() = stream.close()
    }

    protected abstract fun readSample(stream: InputStream): Sample<T>

    protected abstract fun writeSample(sample: Sample<T>, stream: OutputStream)

    public fun delete(): Boolean = file.deleteIfExists()
}
