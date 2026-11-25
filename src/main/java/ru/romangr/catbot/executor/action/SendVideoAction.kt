package ru.romangr.catbot.executor.action

import ru.romangr.catbot.http.JsonHttpClient
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.telegram.model.VideoMessageToSend
import ru.romangr.exceptional.Exceptional

internal class SendVideoAction(httpClient: JsonHttpClient,
                               requestUrl: String,
                               private val fileId: String,
                               override val chat: Chat,
                               errorHandler: ((ExecutionResult) -> Unit)?)
  : AbstractTelegramAction(httpClient, requestUrl, errorHandler) {

  override fun execute(): Exceptional<ExecutionResult> {
    return sendMessageSafely(VideoMessageToSend(chat.id, fileId), VideoMessageToSend.serializer())
  }

  override fun methodName(): String = "sendVideo"

}
