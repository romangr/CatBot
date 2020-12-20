package ru.romangr.catbot.executor.action

import org.springframework.web.client.RestTemplate
import ru.romangr.catbot.telegram.dto.SendVideoResponse
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.DocumentMessageToSend
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.exceptional.Exceptional

internal class SendDocumentAction(restTemplate: RestTemplate,
                                  requestUrl: String,
                                  private val fileId: String,
                                  override val chat: Chat,
                                  errorHandler: ((ExecutionResult) -> Unit)?)
  : AbstractTelegramAction<Message, SendVideoResponse>(restTemplate, requestUrl, errorHandler) {

  override fun execute(): Exceptional<ExecutionResult> {
    return sendMessageSafely(DocumentMessageToSend(chat.id, fileId))
  }

  override fun methodName(): String = "sendDocument"

  override fun responseClass(): Class<SendVideoResponse> = SendVideoResponse::class.java

}
