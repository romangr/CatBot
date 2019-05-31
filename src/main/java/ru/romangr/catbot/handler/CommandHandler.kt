package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.exceptional.Exceptional

abstract class CommandHandler {

    fun handle(chat: Chat, messageText: String): Exceptional<HandlingResult> {
        return if (isApplicable(messageText)) {
            Exceptional.getExceptional {
                HandlingResult(handleCommand(chat, messageText), HandlingStatus.HANDLED)
            }
        } else Exceptional.exceptional(
                HandlingResult(emptyList(), HandlingStatus.SKIPPED)
        )
    }

    internal abstract fun isApplicable(messageText: String): Boolean

    protected abstract fun handleCommand(chat: Chat, messageText: String): List<TelegramAction>
}

fun isMessageNotFromAdmin(adminChatId: Long?, chat: Chat): Boolean {
    return !(adminChatId?.equals(chat.id!!.toLong()) ?: false)
}
