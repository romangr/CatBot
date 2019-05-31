package ru.romangr.catbot.handler

import org.slf4j.LoggerFactory
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat

@StaticCommand(BotCommand.UNSUBSCRIBE)
class UnsubscribeCommandHandler(private val actionFactory: TelegramActionFactory,
                                private val subscribersService: SubscribersService)
    : StaticCommandHandler() {

    override fun handleCommand(chat: Chat, messageText: String): List<TelegramAction> {
        val isDeleted = subscribersService.deleteSubscriber(chat)
        val message: String
        if (isDeleted) {
            message = MESSAGE_TO_UNSUBSCRIBED_ONE
            log.info("Someone have unsubscribed. Total subscribers: {}.", subscribersService.subscribersCount)
        } else {
            message = YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE
        }
        return listOf(actionFactory.newSendMessageAction(chat, message))
    }

    companion object {
        private val log = LoggerFactory.getLogger(UnsubscribeCommandHandler::class.java)
        private const val MESSAGE_TO_UNSUBSCRIBED_ONE = "You have unsubscribed!"
        private const val YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE = "You are not a subscriber yet!"
    }

}
