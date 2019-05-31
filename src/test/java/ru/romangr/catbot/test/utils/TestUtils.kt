package ru.romangr.catbot.test.utils

import org.mockito.ArgumentMatchers
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.exceptional.Exceptional

private val randomChat = Chat(234234)

fun anyChat() = ArgumentMatchers.any(Chat::class.java) ?: randomChat
fun anyTelegramAction() = ArgumentMatchers.any(TelegramAction::class.java)
        ?: object : TelegramAction {
            override fun execute() = Exceptional.exceptional(ExecutionResult.SUCCESS)

            override val chat: Chat
                get() = randomChat
        }
