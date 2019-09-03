package kz.btsd.bot.botsdk

import com.fasterxml.jackson.core.JsonParseException
import kz.btsd.bot.botsdk.exception.RequestFailedException
import kz.btsd.bot.botsdk.service.BotApi
import kz.btsd.messenger.bot.api.model.command.Command
import kz.btsd.messenger.bot.api.model.command.CommandRequest
import kz.btsd.messenger.bot.api.model.update.Update
import mu.KotlinLogging.logger
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.util.*
import java.util.concurrent.ForkJoinPool.commonPool

private const val PERIOD = 1000L
private const val UNAUTHORIZED_CODE = 401

abstract class LongPollingBot(token: String, apiUrl: String) {

    private val log = logger {}
    private val botApi = BotApi.create(apiUrl, token)

    init {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            private var lastUpdateId: String? = null

            override fun run() {
                try {
                    val response = botApi.getUpdates(lastUpdateId).execute()
                    if (response.isSuccessful) {
                        val updates = response.body()!!.updates.filter { it != null }
                        logUpdates(updates)
                        assignLastUpdateId(updates)
                        commonPool().execute { onUpdates(updates) }
                    } else {
                        log.error { "Request for updates failed" }
                        logErrorResponse(response)
                        if (response.code() == UNAUTHORIZED_CODE) {
                            this.cancel()
                        }
                    }
                } catch (e: JsonParseException) {
                    log.error { "Parse error: ${e.requestPayloadAsString}" }
                } catch (e: Exception) {
                    log.error(e) {}
                }
            }

            private fun assignLastUpdateId(updates: List<Update>) {
                updates.lastOrNull()?.let {
                    lastUpdateId = it.updateId
                    log.trace { "lastUpdateId=$lastUpdateId" }
                }
            }
        }, 0, PERIOD)
    }

    open fun onUpdates(updates: List<Update>) {
        updates.forEach(::onUpdate)
    }

    abstract fun onUpdate(update: Update)

    fun sendCommand(command: Command) {
        sendCommandList(listOf(command))
    }

    fun sendCommandList(commandList: List<Command>) {
        log.trace { "Sending commands: $commandList" }
        val commandRequest = CommandRequest(commandList)
        val response = botApi.postCommands(commandRequest).execute()
        if (!response.isSuccessful) {
            log.error { "Commands failed: $commandList" }
            logErrorResponse(response)
            throw RequestFailedException(response, "commands failed: $commandList")
        }
    }

    fun downloadFile(fileId: String): ResponseBody {
        val response = botApi.downloadFile(fileId).execute()
        if (!response.isSuccessful) {
            log.error { "File download failed, fileId: $fileId" }
            logErrorResponse(response)
            throw RequestFailedException(response, "File download failed: $fileId")
        }
        return response.body()!!
    }

    fun uploadFiles(files: Map<File, String>): ResponseBody {
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        files.forEach {
            requestBody.addFormDataPart("file", it.key.name, it.key.asRequestBody(it.value.toMediaTypeOrNull()))
        }

        val response = botApi.uploadFile(requestBody.build()).execute()
        if (!response.isSuccessful) {
            log.error { "Upload failed" }
            logErrorResponse(response)
            throw RequestFailedException(response, "upload failed")
        }

        return response.body()!!
    }

    private fun logUpdates(updates: List<Update>) {
        if (updates.isNotEmpty()) {
            log.trace { "Updates received:" }
            updates.forEach { log.trace { it } }
        }
    }

    private fun logErrorResponse(response: Response<*>) {
        log.error { ("Response: ${response.raw()}") }
        response.body()?.let { log.error { "Body: $it" } }
        response.errorBody()?.let { log.error { "Error body: ${it.string()}" } }
    }
}
