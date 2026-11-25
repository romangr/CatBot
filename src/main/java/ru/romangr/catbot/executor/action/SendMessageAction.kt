package ru.romangr.catbot.executor.action

import ru.romangr.catbot.http.JsonHttpClient
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.telegram.model.TextMessageToSend
import ru.romangr.exceptional.Exceptional

internal class SendMessageAction(httpClient: JsonHttpClient,
                                 requestUrl: String,
                                 private val text: String,
                                 override val chat: Chat,
                                 errorHandler: ((ExecutionResult) -> Unit)?)
  : AbstractTelegramAction(httpClient, requestUrl, errorHandler) {

  override fun execute(): Exceptional<ExecutionResult> {
    return sendMessageSafely(TextMessageToSend(chat, text), TextMessageToSend.serializer())
  }

  override fun methodName(): String = "sendMessage"

}
