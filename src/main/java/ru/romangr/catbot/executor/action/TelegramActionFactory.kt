package ru.romangr.catbot.executor.action

import lombok.RequiredArgsConstructor
import org.springframework.web.client.RestTemplate
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.subscription.MessageToSubscribersType
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult

@RequiredArgsConstructor
class TelegramActionFactory(private val restTemplate: RestTemplate,
                            private val requestUrl: String) {

  fun newSendMessageAction(chat: Chat, text: String): TelegramAction {
    return newSendMessageAction(chat, text, null)
  }

  fun newSendVideoAction(chat: Chat, videoId: String): TelegramAction {
    return newSendVideoAction(chat, videoId, null)
  }

  fun newSendMessageAction(chat: Chat, text: String, errorHandler: ((ExecutionResult) -> Unit)?)
      : TelegramAction {
    return SendMessageAction(restTemplate, requestUrl, text, chat, errorHandler)
  }

  fun newSendVideoAction(chat: Chat, videoId: String, errorHandler: ((ExecutionResult) -> Unit)?)
      : TelegramAction {
    return SendDocumentAction(restTemplate, requestUrl, videoId, chat, errorHandler)
  }

  fun newAction(chat: Chat, message: MessageToSubscribers): TelegramAction =
      newAction(chat, message, null)

  fun newAction(chat: Chat, message: MessageToSubscribers, errorHandler: ((ExecutionResult) -> Unit)?): TelegramAction =
      when (message.type) {
        MessageToSubscribersType.TEXT -> newSendMessageAction(chat, message.text!!, errorHandler)
        MessageToSubscribersType.DOCUMENT -> newSendVideoAction(chat, message.documentId!!, errorHandler)
      }

}
