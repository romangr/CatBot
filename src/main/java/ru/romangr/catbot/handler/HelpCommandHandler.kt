package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.statistic.StatisticService
import ru.romangr.catbot.telegram.model.Chat

@StaticCommand(BotCommand.HELP)
class HelpCommandHandler(private val actionFactory: TelegramActionFactory,
                         timeToSendMessageToSubscribers: Int,
                         statisticService: StatisticService)
    : StaticCommandHandler(statisticService) {

    private val helpMessage =
            """/cat to get a random cat 🐱
/subscribe to get a random cat every day 🐈 ($timeToSendMessageToSubscribers:00 UTC)"""

    override fun handleStringCommand(chat: Chat, text: String): List<TelegramAction> {
        return listOf(actionFactory.newSendMessageAction(chat, helpMessage))
    }

}
