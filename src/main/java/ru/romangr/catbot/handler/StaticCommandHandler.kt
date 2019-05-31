package ru.romangr.catbot.handler

import org.apache.commons.lang3.StringUtils
import java.util.*

abstract class StaticCommandHandler : CommandHandler() {

    private val command: String =
            Optional.ofNullable(this.javaClass.getAnnotation(StaticCommand::class.java))
            .map{ it.value }
            .map{ it.command }
            .orElseThrow { IllegalStateException("No annotation StaticCommand found on " + this.javaClass.canonicalName) }

    override fun isApplicable(messageText: String): Boolean {
        return StringUtils.equalsIgnoreCase(messageText, command)
    }
}
