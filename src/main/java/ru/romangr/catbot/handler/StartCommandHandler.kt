package ru.romangr.catbot.handler

import lombok.RequiredArgsConstructor
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.telegram.model.Chat

@StaticCommand(BotCommand.START)
@RequiredArgsConstructor
class StartCommandHandler(private val actionFactory: TelegramActionFactory)
    : StaticCommandHandler() {

    override fun handleCommand(chat: Chat, messageText: String): List<TelegramAction> {
        return listOf(actionFactory.newSendMessageAction(chat, HELP_STRING))
    }

    companion object {
        private const val HELP_STRING = 
        """/cat to get a random cat 🐱
/subscribe to get a random cat every day 🐈"""
    }
}
