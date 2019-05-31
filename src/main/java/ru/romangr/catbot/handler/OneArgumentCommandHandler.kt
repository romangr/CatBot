package ru.romangr.catbot.handler

import org.apache.commons.lang3.StringUtils
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.telegram.model.Chat
import java.util.Optional

abstract class OneArgumentCommandHandler : CommandHandler() {

    private val command: String =
            Optional.ofNullable(this.javaClass.getAnnotation(OneArgumentCommand::class.java))
                    .map { it.value }
                    .map { it.command }
                    .orElseThrow {
                        IllegalStateException("No annotation OneArgumentCommand found on "
                                + this.javaClass.canonicalName)
                    }

    override fun handleCommand(chat: Chat, messageText: String): List<TelegramAction> {
        val argument = StringUtils.removeStartIgnoreCase(messageText, this.command).trim()
        return handleCommandWithArgument(chat, argument)
    }

    abstract fun handleCommandWithArgument(chat: Chat, argument: String): List<TelegramAction>

    override fun isApplicable(messageText: String): Boolean {
        return StringUtils.startsWithIgnoreCase(messageText, command)
    }
}
