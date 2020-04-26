package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.exceptional.Exceptional

abstract class CommandHandler {

    fun handle(chat: Chat, message: Message): Exceptional<HandlingResult> {
        return if (isApplicable(message)) {
            Exceptional.getExceptional {
                HandlingResult(handleCommand(chat, message), HandlingStatus.HANDLED)
            }
        } else Exceptional.exceptional(
                HandlingResult(emptyList(), HandlingStatus.SKIPPED)
        )
    }

    internal abstract fun isApplicable(message: Message): Boolean

    protected abstract fun handleCommand(chat: Chat, message: Message): List<TelegramAction>
}

fun isMessageNotFromAdmin(adminChatId: Long?, chat: Chat): Boolean {
    return !(adminChatId?.equals(chat.id.toLong()) ?: false)
}
