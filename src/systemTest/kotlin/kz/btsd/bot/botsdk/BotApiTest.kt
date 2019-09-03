package kz.btsd.bot.botsdk

import kz.btsd.bot.botsdk.service.BotApi
import mu.KLogging
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test


class BotApiTest : AbstractTest() {

    companion object : KLogging()

    private val botApi = BotApi.create(apiUrl, token)

    @Test
    fun `test getUpdates`() {
        val response = botApi.getUpdates().execute()
        logger.info { "Response: $response" }
        assertNotNull(response)
    }

}
