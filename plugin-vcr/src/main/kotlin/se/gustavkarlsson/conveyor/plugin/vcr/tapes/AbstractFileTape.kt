package se.gustavkarlsson.conveyor.plugin.vcr.tapes

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import se.gustavkarlsson.conveyor.plugin.vcr.WriteableTape
import java.io.File
import java.io.InputStream
import java.io.OutputStream

public abstract class AbstractFileTape<T>(
    private val file: File,
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ReadableTape<T>, WriteableTape<T> {
    final override fun openForReading(): ReadableTape.Reading<T> = Reading()

    final override fun openForWriting(): WriteableTape.Writing<T> = Writing()

    private inner class Reading : ReadableTape.Reading<T> {
        private val stream = file.inputStream().buffered(bufferSize)

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
}
