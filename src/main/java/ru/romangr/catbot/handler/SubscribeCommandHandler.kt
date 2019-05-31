package ru.romangr.catbot.handler

import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat

@Slf4j
@StaticCommand(BotCommand.SUBSCRIBE)
class SubscribeCommandHandler(private val actionFactory: TelegramActionFactory,
                                       private val subscribersService: SubscribersService)
    : StaticCommandHandler() {

    private val log = LoggerFactory.getLogger(CatCommandHandler::class.java)

    override fun handleCommand(chat: Chat, messageText: String): List<TelegramAction> {
        val isAdded = subscribersService.addSubscriber(chat)
        val message: String
        if (isAdded) {
            message = MESSAGE_TO_NEW_SUBSCRIBER
            log.info("New subscriber. Total subscribers: {}.", subscribersService.subscribersCount)
        } else {
            message = YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE
        }
        return listOf(actionFactory.newSendMessageAction(chat, message))
    }

    companion object {

        private const val MESSAGE_TO_NEW_SUBSCRIBER = "Thank you for subscription!"
        private const val YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE = "You have already subscribed!"
    }

}
