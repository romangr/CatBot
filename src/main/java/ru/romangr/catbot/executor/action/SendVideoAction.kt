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
                               private val fileId: String,
                               override val chat: Chat,
                               errorHandler: ((ExecutionResult) -> Unit)?)
  : AbstractTelegramAction<Message, SendVideoResponse>(restTemplate, requestUrl, errorHandler) {

  override fun execute(): Exceptional<ExecutionResult> {
    return sendMessageSafely(VideoMessageToSend(chat.id, fileId))
  }

  override fun methodName(): String = "sendVideo"

  override fun responseClass(): Class<SendVideoResponse> = SendVideoResponse::class.java

}
