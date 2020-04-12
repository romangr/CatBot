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
        return listOf(actionFactory.newSendMessageAction(chat, START_MESSAGE))
    }

    companion object {
        private const val START_MESSAGE = """Hi, let's see what cats we have for today!
/cat to get a random cat 🐱
/subscribe to get a random cat every day 🐈
If you like it — share this bot with your friends!"""
    }
}
