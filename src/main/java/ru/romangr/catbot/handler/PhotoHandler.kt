package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.statistic.StatisticService
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.Message

class PhotoHandler(
    private val actionFactory: TelegramActionFactory,
    private val subscribersService: SubscribersService,
    private val adminChatId: Long?,
    statisticService: StatisticService
) : CommandHandler(statisticService) {

    override fun isApplicable(message: Message): Boolean {
        return !message.photo.isNullOrEmpty()
    }

    override fun handleCommand(chat: Chat, message: Message): List<TelegramAction> {
        if (isMessageNotFromAdmin(this.adminChatId, chat)) {
            return emptyList()
        }
        if (message.photo.isNullOrEmpty()) {
            throw IllegalArgumentException("Photo size list is null or empty")
        }
        val photoSize = message.photo.maxBy { photoSize -> photoSize.width }!!
        val photoMessage = MessageToSubscribers.photoMessage(photoSize.fileId, message.caption)
        subscribersService.addMessageToSubscribers(photoMessage)
        val text = "Photo is added to the queue, there are ${subscribersService.messageQueueLength} message(s)"
        return listOf(actionFactory.newSendMessageAction(chat, text))
    }
}
