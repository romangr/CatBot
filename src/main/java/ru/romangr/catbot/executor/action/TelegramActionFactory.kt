package ru.romangr.catbot.executor.action

import lombok.RequiredArgsConstructor
import org.springframework.web.client.RestTemplate
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.subscription.MessageToSubscribersType
import ru.romangr.catbot.telegram.model.Chat

@RequiredArgsConstructor
class TelegramActionFactory(private val restTemplate: RestTemplate,
                            private val requestUrl: String) {

    fun newSendMessageAction(chat: Chat, text: String): TelegramAction {
        return SendMessageAction(restTemplate, requestUrl, text, chat)
    }

    fun newSendVideoAction(chat: Chat, videoId: String): TelegramAction {
        return SendDocumentAction(restTemplate, requestUrl, videoId, chat)
    }

    fun newAction(chat: Chat, message: MessageToSubscribers): TelegramAction =
            when (message.type) {
                MessageToSubscribersType.TEXT -> newSendMessageAction(chat, message.text!!)
                MessageToSubscribersType.DOCUMENT -> newSendVideoAction(chat, message.documentId!!)
            }

}
