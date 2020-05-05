package ru.romangr.catbot.handler

import org.apache.commons.lang3.StringUtils
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.statistic.StatisticService
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.Message
import java.util.*

abstract class OneArgumentCommandHandler(statisticService: StatisticService)
    : CommandHandler(statisticService) {

    private val command: String =
            Optional.ofNullable(this.javaClass.getAnnotation(OneArgumentCommand::class.java))
                    .map { it.value }
                    .map { it.command }
                    .orElseThrow {
                        IllegalStateException("No annotation OneArgumentCommand found on "
                                + this.javaClass.canonicalName)
                    }

    override fun handleCommand(chat: Chat, message: Message): List<TelegramAction> {
        val messageText = message.text
        val argument = StringUtils.removeStartIgnoreCase(messageText, this.command).trim()
        return handleCommandWithArgument(chat, argument)
    }

    abstract fun handleCommandWithArgument(chat: Chat, argument: String): List<TelegramAction>

    override fun isApplicable(message: Message): Boolean {
        return message.text != null && StringUtils.startsWithIgnoreCase(message.text, command)
    }
}
