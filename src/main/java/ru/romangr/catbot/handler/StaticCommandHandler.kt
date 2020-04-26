package ru.romangr.catbot.handler

import org.apache.commons.lang3.StringUtils
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.Message
import java.util.*

abstract class StaticCommandHandler : CommandHandler() {

    private val command: String =
            Optional.ofNullable(this.javaClass.getAnnotation(StaticCommand::class.java))
            .map{ it.value }
            .map{ it.command }
            .orElseThrow { IllegalStateException("No annotation StaticCommand found on " + this.javaClass.canonicalName) }

    abstract fun handleStringCommand(chat: Chat, text: String): List<TelegramAction>

    override fun isApplicable(message: Message): Boolean {
        return message.text != null && StringUtils.equalsIgnoreCase(message.text, command)
    }

    override fun handleCommand(chat: Chat, message: Message): List<TelegramAction> {
        return handleStringCommand(chat, message.text!!)
    }
}
