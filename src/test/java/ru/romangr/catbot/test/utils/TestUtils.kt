package ru.romangr.catbot.test.utils

import org.mockito.ArgumentMatchers
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.catbot.telegram.model.User
import ru.romangr.exceptional.Exceptional

private val randomChat = Chat(234234)
private val randomMessage = Message(213, User.builder().id(123).build(), Chat(123), null, null)

fun anyChat() = ArgumentMatchers.any(Chat::class.java) ?: randomChat
fun anyTelegramAction() = ArgumentMatchers.any(TelegramAction::class.java)
        ?: object : TelegramAction {
            override fun execute() = Exceptional.exceptional(ExecutionResult.SUCCESS)

            override val chat: Chat
                get() = randomChat
        }
fun anyMessage() = ArgumentMatchers.any(Message::class.java) ?: randomMessage
