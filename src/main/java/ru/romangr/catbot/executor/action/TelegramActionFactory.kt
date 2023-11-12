package ru.romangr.catbot.executor.action

import lombok.RequiredArgsConstructor
import org.springframework.web.client.RestTemplate
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.subscription.MessageToSubscribersType
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult

@RequiredArgsConstructor
class TelegramActionFactory(
    private val restTemplate: RestTemplate,
    private val requestUrl: String
) {

    fun newSendMessageAction(chat: Chat, text: String): TelegramAction {
        return newSendMessageAction(chat, text, null)
    }

    fun newSendMessageAction(chat: Chat, text: String, errorHandler: ((ExecutionResult) -> Unit)?)
            : TelegramAction {
        return SendMessageAction(restTemplate, requestUrl, text, chat, errorHandler)
    }

    fun newSendDocumentAction(chat: Chat, documentId: String, errorHandler: ((ExecutionResult) -> Unit)?)
            : TelegramAction {
        return SendDocumentAction(restTemplate, requestUrl, documentId, chat, errorHandler)
    }

    fun newSendVideoAction(chat: Chat, documentId: String, errorHandler: ((ExecutionResult) -> Unit)?)
            : TelegramAction {
        return SendVideoAction(restTemplate, requestUrl, documentId, chat, errorHandler)
    }

    fun newSendPhotoAction(chat: Chat, documentId: String, caption: String?, errorHandler: ((ExecutionResult) -> Unit)?)
            : TelegramAction {
        return SendPhotoAction(restTemplate, requestUrl, documentId, caption, chat, errorHandler)
    }

    fun newAction(chat: Chat, message: MessageToSubscribers): TelegramAction =
        newAction(chat, message, null)

    fun newAction(
        chat: Chat,
        message: MessageToSubscribers,
        errorHandler: ((ExecutionResult) -> Unit)?
    ): TelegramAction =
        when (message.type) {
            MessageToSubscribersType.TEXT -> newSendMessageAction(chat, message.text!!, errorHandler)
            MessageToSubscribersType.DOCUMENT -> newSendDocumentAction(chat, message.documentId!!, errorHandler)
            MessageToSubscribersType.VIDEO -> newSendVideoAction(chat, message.videoId!!, errorHandler)
            MessageToSubscribersType.PHOTO -> newSendPhotoAction(chat, message.photoId!!, message.text, errorHandler)
        }

}
