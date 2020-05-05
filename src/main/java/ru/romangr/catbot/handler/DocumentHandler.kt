package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.Message

class DocumentHandler(private val actionFactory: TelegramActionFactory,
                      private val subscribersService: SubscribersService,
                      private val adminChatId: Long?) : CommandHandler() {

    override fun isApplicable(message: Message): Boolean {
        return message.document != null
    }

    override fun handleCommand(chat: Chat, message: Message): List<TelegramAction> {
        if (isMessageNotFromAdmin(this.adminChatId, chat)) {
            return emptyList()
        }
        val documentMessage = MessageToSubscribers.documentMessage(message.document!!.fileId)
        subscribersService.addMessageToSubscribers(documentMessage)
        val text = "Document is added to the queue, there are ${subscribersService.messageQueueLength} message(s)"
        return listOf(actionFactory.newSendMessageAction(chat, text))
    }
}
