package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.statistic.StatisticService
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.Message

class VideoHandler(
    private val actionFactory: TelegramActionFactory,
    private val subscribersService: SubscribersService,
    private val adminChatId: Long?,
    statisticService: StatisticService
) : CommandHandler(statisticService) {

    override fun isApplicable(message: Message): Boolean {
        return message.video != null
    }

    override fun handleCommand(chat: Chat, message: Message): List<TelegramAction> {
        if (isMessageNotFromAdmin(this.adminChatId, chat)) {
            return emptyList()
        }
        val videoMessage = MessageToSubscribers.videoMessage(message.video!!.fileId)
        subscribersService.addMessageToSubscribers(videoMessage)
        val text = "Video is added to the queue, there are ${subscribersService.messageQueueLength} message(s)"
        return listOf(actionFactory.newSendMessageAction(chat, text))
    }
}
