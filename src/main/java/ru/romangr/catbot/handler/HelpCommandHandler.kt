package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.telegram.model.Chat

@StaticCommand(BotCommand.HELP)
class HelpCommandHandler(private val actionFactory: TelegramActionFactory)
    : StaticCommandHandler() {

    override fun handleCommand(chat: Chat, messageText: String): List<TelegramAction> {
        return listOf(actionFactory.newSendMessageAction(chat, HELP_STRING))
    }

    companion object {
        private const val HELP_STRING = "Type /cat to get a random cat :3"
    }
}
