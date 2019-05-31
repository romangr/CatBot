package ru.romangr.catbot.telegram

import ru.romangr.catbot.executor.TelegramActionExecutor
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.utils.PropertiesResolver
import java.util.Optional

class TelegramAdminNotifier(private val actionFactory: TelegramActionFactory,
                            private val actionExecutor: TelegramActionExecutor,
                            private val propertiesResolver: PropertiesResolver,
                            private val adminChatId: Long?) {

    fun botStarted() {
        Optional.ofNullable(adminChatId)
                .map { telegramAction(it) }
                .ifPresent { actionExecutor.execute(listOf(it)) }
    }

    private fun telegramAction(it: Long) =
            actionFactory.newSendMessageAction(Chat(it.toInt()), "Bot started! ${propertiesResolver.buildInfo}")

}
