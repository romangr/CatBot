package ru.romangr.catbot.handler

import lombok.RequiredArgsConstructor
import org.slf4j.LoggerFactory
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat

@StaticCommand(BotCommand.SEND_MESSAGE_TO_SUBSCRIBERS)
@RequiredArgsConstructor
class SendMessageToSubscribersCommandHandler(private val subscribersService: SubscribersService,
                                             private val adminChatId: Long?)
    : StaticCommandHandler() {

    override fun handleCommand(chat: Chat, messageText: String): List<TelegramAction> {
        if (isMessageNotFromAdmin(this.adminChatId, chat)) {
            return emptyList()
        }
        return subscribersService.sendMessageToSubscribers()
                .ifException { e -> log.warn("Exception during sending message to subscribers", e) }
                .getOrDefault(emptyList())
    }

    companion object {
        private val log = LoggerFactory.getLogger(SendMessageToSubscribersCommandHandler::class.java)
    }

}
