package ru.romangr.catbot.executor.action

import ru.romangr.catbot.http.JsonHttpClient
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.DocumentMessageToSend
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.exceptional.Exceptional

internal class SendDocumentAction(httpClient: JsonHttpClient,
                                  requestUrl: String,
                                  private val fileId: String,
                                  override val chat: Chat,
                                  errorHandler: ((ExecutionResult) -> Unit)?)
  : AbstractTelegramAction(httpClient, requestUrl, errorHandler) {

  override fun execute(): Exceptional<ExecutionResult> {
    return sendMessageSafely(DocumentMessageToSend(chat.id, fileId), DocumentMessageToSend.serializer())
  }

  override fun methodName(): String = "sendDocument"

}
