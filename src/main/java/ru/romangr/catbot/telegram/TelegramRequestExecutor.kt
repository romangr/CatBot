package ru.romangr.catbot.telegram

import kotlinx.serialization.builtins.ListSerializer
import org.slf4j.LoggerFactory
import ru.romangr.catbot.http.JsonHttpClient
import ru.romangr.catbot.telegram.dto.Bot
import ru.romangr.catbot.telegram.dto.TelegramResponse
import ru.romangr.catbot.telegram.model.Update
import ru.romangr.catbot.utils.URLBuilder
import ru.romangr.exceptional.Exceptional
import java.io.IOException

class TelegramRequestExecutor(
    private val httpClient: JsonHttpClient,
    private val requestUrl: String,
) {

    fun getMe(): Bot? = httpClient.get(methodUrl(GET_ME_METHOD).build(), GET_ME_RESPONSE).result

    fun getUpdates(offset: Long): Exceptional<List<Update>> =
        Exceptional.getExceptional {
            httpClient.get(
                methodUrl(GET_UPDATES_METHOD).withParameter(OFFSET_PARAMETER, offset).build(),
                GET_UPDATES_RESPONSE
            )
        }.safelyMap { response ->
            if (!response.isOk) {
                throw RuntimeException("Error during getting updates")
            }
            response.result.orEmpty()
        }

    val isConnectedToInternet: Boolean
        get() = try {
            httpClient.get(methodUrl(GET_ME_METHOD).build(), GET_ME_RESPONSE)
            log.debug("Connected to the Internet")
            true
        } catch (e: IOException) {
            log.warn("No connection", e)
            false
        } catch (e: Exception) {
            log.error("Exception during connectivity check", e)
            false
        }

    private fun methodUrl(method: String) = URLBuilder().withHost(requestUrl).withPath(method)

    companion object {
        private val log = LoggerFactory.getLogger(TelegramRequestExecutor::class.java)
        private const val GET_ME_METHOD = "getMe"
        private const val GET_UPDATES_METHOD = "getUpdates"
        private const val OFFSET_PARAMETER = "offset"
        private val GET_ME_RESPONSE = TelegramResponse.serializer(Bot.serializer())
        private val GET_UPDATES_RESPONSE = TelegramResponse.serializer(ListSerializer(Update.serializer()))
    }
}
