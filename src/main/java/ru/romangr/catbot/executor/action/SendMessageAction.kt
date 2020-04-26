package ru.romangr.catbot.executor.action

import org.springframework.web.client.RestTemplate
import ru.romangr.catbot.telegram.dto.SendMessageResponse
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.catbot.telegram.model.TextMessageToSend
import ru.romangr.exceptional.Exceptional

internal class SendMessageAction(restTemplate: RestTemplate,
                                 requestUrl: String,
                                 private val text: String,
                                 override val chat: Chat)
    : AbstractTelegramAction<Message, SendMessageResponse>(restTemplate, requestUrl) {

    override fun execute(): Exceptional<ExecutionResult> {
        return sendMessageSafely(TextMessageToSend(chat, text))
    }

    override fun methodName(): String = "sendMessage"

    override fun responseClass(): Class<SendMessageResponse> = SendMessageResponse::class.java

}
