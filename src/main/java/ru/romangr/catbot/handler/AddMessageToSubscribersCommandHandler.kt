package ru.romangr.catbot.handler

import org.apache.commons.lang3.StringUtils
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat

@OneArgumentCommand(BotCommand.ADD_MESSAGE_TO_SUBSCRIBERS)
class AddMessageToSubscribersCommandHandler(private val subscribersService: SubscribersService,
                                            private val actionFactory: TelegramActionFactory,
                                            private val adminChatId: Long?)
    : OneArgumentCommandHandler() {

    override fun handleCommandWithArgument(chat: Chat, argument: String): List<TelegramAction> {
        if (isMessageNotFromAdmin(this.adminChatId, chat)) {
            return emptyList()
        }
        if (StringUtils.isBlank(argument)) {
            return listOf(actionFactory.newSendMessageAction(chat, "Argument is empty or blank"))
        }
        subscribersService.addMessageToSubscribers(MessageToSubscribers.textMessage(argument))
        val text = "Message is added to the queue, there are ${subscribersService.messageQueueLength} message(s)"
        return listOf(actionFactory.newSendMessageAction(chat, text))
    }
}
