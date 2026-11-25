package ru.romangr.catbot.executor.action

import ru.romangr.catbot.http.JsonHttpClient
import ru.romangr.catbot.telegram.dto.SendVideoResponse
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.catbot.telegram.model.VideoMessageToSend
import ru.romangr.exceptional.Exceptional

internal class SendVideoAction(httpClient: JsonHttpClient,
                               requestUrl: String,
                               private val fileId: String,
                               override val chat: Chat,
                               errorHandler: ((ExecutionResult) -> Unit)?)
  : AbstractTelegramAction<Message, SendVideoResponse>(httpClient, requestUrl, errorHandler) {

  override fun execute(): Exceptional<ExecutionResult> {
    return sendMessageSafely(VideoMessageToSend(chat.id, fileId))
  }

  override fun methodName(): String = "sendVideo"

  override fun responseClass(): Class<SendVideoResponse> = SendVideoResponse::class.java

}
