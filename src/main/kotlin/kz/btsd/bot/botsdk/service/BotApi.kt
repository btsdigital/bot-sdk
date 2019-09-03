package kz.btsd.bot.botsdk.service

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_INVALID_SUBTYPE
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.TimeUnit.SECONDS
import kz.btsd.messenger.bot.api.model.command.CommandRequest
import kz.btsd.messenger.bot.api.model.update.UpdateResponse
import mu.KotlinLogging
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val BOT_TOKEN_HEADER = "X-BOT-TOKEN"
private val log = KotlinLogging.logger {}
private const val TIMEOUT = 60L

interface BotApi {
    @GET("updates")
    fun getUpdates(@Query("lastUpdateId") lastUpdateId: String? = null): Call<UpdateResponse>

    @POST("updates")
    fun postCommands(@Body commandRequest: CommandRequest): Call<Void>

    @GET("download")
    fun downloadFile(@Query("fileId") fileId: String): Call<ResponseBody>

    @POST("upload")
    fun uploadFile(@Body body: RequestBody): Call<ResponseBody>

    companion object {
        fun create(apiUrl: String, botToken: String): BotApi = Retrofit.Builder()
                .baseUrl(apiUrl)
                .client(OkHttpClient.Builder()
                        .baseHeaders(mapOf(BOT_TOKEN_HEADER to botToken))
                        .addInterceptor(getLogging())
                        .readTimeout(TIMEOUT, SECONDS)
                        .build())
                .addConverterFactory(JacksonConverterFactory.create(
                        ObjectMapper().findAndRegisterModules().apply {
                            configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                            configure(FAIL_ON_INVALID_SUBTYPE, false)
                        }
                ))
                .build()
                .create(BotApi::class.java)

        private fun getLogging(): HttpLoggingInterceptor {
            val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
                this.level = getHttpLoggingInterceptorLevel() }
            loggingInterceptor.redactHeader(BOT_TOKEN_HEADER)
            return loggingInterceptor
        }

        private fun getHttpLoggingInterceptorLevel() = when {
            log.isTraceEnabled -> HttpLoggingInterceptor.Level.BODY
            log.isDebugEnabled -> HttpLoggingInterceptor.Level.BASIC
            else -> HttpLoggingInterceptor.Level.NONE
        }
    }
}

fun OkHttpClient.Builder.baseHeaders(headers: Map<String, String>): OkHttpClient.Builder {
    return addInterceptor { chain: Interceptor.Chain ->
        val request = chain.request().newBuilder()
                .apply {
                    headers.forEach { (header, value) -> addHeader(header, value) }
                }
                .build()
        chain.proceed(request)
    }
}
