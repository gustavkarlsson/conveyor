package se.gustavkarlsson.conveyor.demo

import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private val DELIMITERS = Regex("[._-]+")

@Suppress("MagicNumber")
object Api {
    suspend fun login(email: String, password: String): String? {
        delay(Random.nextLong(500, 2000))
        if (password.none { it.isDigit() }) return null
        return email.substringBefore('@')
            .replace(DELIMITERS, " ")
            .capitalizeWords()
    }

    fun operation(): Flow<Float> = flow {
        var progress = 0F
        while (progress < 1F) {
            delay(Random.nextLong(200))
            progress += Random.nextFloat() * 0.05F
            emit(progress)
        }
    }
}

private fun String.capitalizeWords(): String =
    split(" ")
        .joinToString(" ") { word ->
            word.capitalize()
        }

private fun String.capitalize(): String = replaceFirstChar { char ->
    if (char.isLowerCase()) {
        char.titlecase(Locale.getDefault())
    } else char.toString()
}
