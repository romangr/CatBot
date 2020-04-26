package ru.romangr.catbot.executor.action

import org.springframework.web.client.RestTemplate
import ru.romangr.catbot.telegram.dto.SendVideoResponse
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.catbot.telegram.model.VideoMessageToSend
import ru.romangr.exceptional.Exceptional

internal class SendVideoAction(restTemplate: RestTemplate,
                               requestUrl: String,
                               private val videoId: String,
                               override val chat: Chat)
    : AbstractTelegramAction<Message, SendVideoResponse>(restTemplate, requestUrl) {

    override fun execute(): Exceptional<ExecutionResult> {
        return sendMessageSafely(VideoMessageToSend(chat.id, videoId))
    }

    override fun methodName(): String = "sendVideo"

    override fun responseClass(): Class<SendVideoResponse> = SendVideoResponse::class.java

}
