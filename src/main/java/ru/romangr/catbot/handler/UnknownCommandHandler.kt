package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.exceptional.Exceptional

class UnknownCommandHandler(private val actionFactory: TelegramActionFactory) {

    fun handle(chat: Chat): Exceptional<HandlingResult> {
        val action = actionFactory.newSendMessageAction(chat, "Incorrect command syntax")
        return Exceptional.exceptional(HandlingResult(listOf(action), HandlingStatus.HANDLED))
    }

}
