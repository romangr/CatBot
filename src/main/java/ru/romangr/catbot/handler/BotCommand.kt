package ru.romangr.catbot.handler

import java.util.*
import java.util.stream.Collectors

enum class BotCommand(val command: String) {
    START("/start"),
    CAT("/cat"),
    SUBSCRIBE("/subscribe"),
    UNSUBSCRIBE("/unsubscribe"),
    HELP("/help"),
    SEND_MESSAGE_TO_SUBSCRIBERS("/smts");


    companion object {
        init {
            // check no duplications in commands
            val uniqueCommands = Arrays.stream(BotCommand.values())
                    .collect(Collectors.toSet())
            if (uniqueCommands.size < BotCommand.values().size) {
                throw IllegalStateException("Commands are not unique")
            }
        }
    }
}
