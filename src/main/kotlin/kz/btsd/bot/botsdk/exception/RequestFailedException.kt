package kz.btsd.bot.botsdk.exception

import retrofit2.Response

class RequestFailedException(val response: Response<out Any>, message: String) : RuntimeException(message)
