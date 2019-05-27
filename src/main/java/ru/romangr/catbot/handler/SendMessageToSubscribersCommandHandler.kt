package ru.romangr.catbot.handler

import lombok.RequiredArgsConstructor
import org.slf4j.LoggerFactory
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat

@StaticCommand(BotCommand.SEND_MESSAGE_TO_SUBSCRIBERS)
@RequiredArgsConstructor
class SendMessageToSubscribersCommandHandler(private val actionFactory: TelegramActionFactory,
                                             private val subscribersService: SubscribersService,
                                             private val adminChatId: Long?)
    : StaticCommandHandler() {

    override fun handleCommand(chat: Chat, messageText: String): List<TelegramAction> {
        if (isMessageFromAdmin(chat)) {
            return subscribersService.sendMessageToSubscribers()
                    .ifException { e -> log.warn("Exception during sending message to subscribers", e) }
                    .getOrDefault(emptyList())
        }
        return listOf(actionFactory.newSendMessageAction(chat, "No permission to execute"))
    }

    private fun isMessageFromAdmin(chat: Chat): Boolean {
        return adminChatId?.equals(chat.id!!.toLong()) ?: false
    }

    companion object {
        private val log = LoggerFactory.getLogger(SendMessageToSubscribersCommandHandler::class.java)
    }

}
