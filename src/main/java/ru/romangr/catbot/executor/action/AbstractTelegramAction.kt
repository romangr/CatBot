package ru.romangr.catbot.executor.action

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlinx.serialization.SerializationStrategy
import ru.romangr.catbot.http.HttpStatusException
import ru.romangr.catbot.http.JsonHttpClient
import ru.romangr.catbot.telegram.dto.TelegramResponse
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.utils.URLBuilder
import ru.romangr.exceptional.Exceptional

abstract class AbstractTelegramAction(
    private val httpClient: JsonHttpClient,
    private val requestUrl: String,
    private val errorHandler: ((ExecutionResult) -> Unit)?) : TelegramAction {

  abstract fun methodName(): String

  internal fun <B> sendMessageSafely(dto: B, serializer: SerializationStrategy<B>): Exceptional<ExecutionResult> {
    return Exceptional.getExceptional {
      val url = URLBuilder().withHost(requestUrl).withPath(methodName()).build()
      httpClient.post(url, dto, serializer, RESPONSE_DESERIALIZER)
    }
        .map<Message> { it?.result }
        .map { ExecutionResult.SUCCESS }
        .resumeOnException { e ->
          if (e is HttpStatusException) {
            val body = e.responseBody
            when {
              e.statusCode == TOO_MANY_REQUESTS -> return@resumeOnException ExecutionResult.RATE_LIMIT_FAILURE
              e.statusCode == FORBIDDEN && body.contains("bot was blocked by the user") ->
                return@resumeOnException ExecutionResult.BOT_IS_BLOCKED_BY_USER
              e.statusCode == FORBIDDEN && body.contains("user is deactivated") ->
                return@resumeOnException ExecutionResult.USER_IS_DEACTIVATED
              e.statusCode == FORBIDDEN && body.contains("bot was kicked from the group chat") ->
                return@resumeOnException ExecutionResult.BOT_KICKED_FROM_GROUP_CHAT
              e.statusCode == BAD_REQUEST && body.contains("chat not found") ->
                return@resumeOnException ExecutionResult.CHAT_NOT_FOUND
            }
            log.warn("Failure during action execution: {}, response body: {}", e.statusCode, body)
            return@resumeOnException ExecutionResult.FAILURE
          }
          log.warn("Failure during action execution", e)
          ExecutionResult.FAILURE
        }
        .ifValue {
          if (it != ExecutionResult.SUCCESS) {
            errorHandler?.invoke(it)
          }
        }
  }

  companion object {
    private val RESPONSE_DESERIALIZER = TelegramResponse.serializer(Message.serializer())
    private const val BAD_REQUEST = 400
    private const val FORBIDDEN = 403
    private const val TOO_MANY_REQUESTS = 429
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
