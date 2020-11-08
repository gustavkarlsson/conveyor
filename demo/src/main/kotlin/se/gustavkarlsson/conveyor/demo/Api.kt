package se.gustavkarlsson.conveyor.demo

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

private val DELIMITERS = Regex("[._-]+")

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
    split(" ").joinToString(" ") { it.capitalize() }
