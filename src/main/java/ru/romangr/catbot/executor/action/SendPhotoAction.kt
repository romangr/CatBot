package ru.romangr.catbot.executor.action

import ru.romangr.catbot.http.JsonHttpClient
import ru.romangr.catbot.telegram.dto.SendVideoResponse
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.catbot.telegram.model.PhotoMessageToSend
import ru.romangr.exceptional.Exceptional

internal class SendPhotoAction(httpClient: JsonHttpClient,
                               requestUrl: String,
                               private val fileId: String,
                               override val chat: Chat,
                               errorHandler: ((ExecutionResult) -> Unit)?)
  : AbstractTelegramAction<Message, SendVideoResponse>(httpClient, requestUrl, errorHandler) {

  override fun execute(): Exceptional<ExecutionResult> {
    return sendMessageSafely(PhotoMessageToSend(chat.id, fileId))
  }

  override fun methodName(): String = "sendPhoto"

  override fun responseClass(): Class<SendVideoResponse> = SendVideoResponse::class.java

}
