package kz.btsd.bot.botsdk

import mu.KLogging
import java.lang.IllegalArgumentException

abstract class AbstractTest {
    companion object : KLogging()

    var apiUrl: String = System.getenv("url") ?: throw IllegalArgumentException()
    var token: String = System.getenv("token") ?: throw IllegalArgumentException()

    init {
        logger.info { "apiUrl set to: $apiUrl" }
        logger.info { "token set to: ${token.replaceLastSymbols()}" }
    }
}

/**
 * For security reasons
 */
fun String.replaceLastSymbols() =  this.dropLast(6) + "******"
