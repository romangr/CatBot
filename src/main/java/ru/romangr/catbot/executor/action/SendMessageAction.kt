package ru.romangr.catbot.executor.action

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import ru.romangr.catbot.telegram.dto.SendMessageResponse
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.catbot.telegram.model.MessageToSend
import ru.romangr.catbot.utils.URLBuilder
import ru.romangr.exceptional.Exceptional

internal class SendMessageAction(private val restTemplate: RestTemplate,
                                 private val requestUrl: String,
                                 private val text: String,
                                 override val chat: Chat) : TelegramAction {

    override fun execute(): Exceptional<ExecutionResult> {
        return sendMessageSafely(chat, text)
    }

    private fun sendMessageSafely(chat: Chat, message: String): Exceptional<ExecutionResult> {
        return Exceptional.getExceptional {
            val url = URLBuilder().withHost(requestUrl).withPath("sendMessage").build()
            restTemplate
                    .postForObject(url, MessageToSend(chat, message), SendMessageResponse::class.java)
        }
                .map<Message> { it?.result }
                .map { ExecutionResult.SUCCESS }
                .resumeOnException { e ->
                    if (e is HttpClientErrorException && isTooManyRequestStatus(e)) {
                        return@resumeOnException ExecutionResult.RATE_LIMIT_FAILURE
                    }
                    ExecutionResult.FAILURE
                }
    }

    private fun isTooManyRequestStatus(e: Exception): Boolean {
        return (e as HttpClientErrorException).statusCode == HttpStatus.TOO_MANY_REQUESTS
    }

}
