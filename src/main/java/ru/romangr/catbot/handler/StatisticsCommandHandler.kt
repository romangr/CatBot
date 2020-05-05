package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.statistic.StatisticService
import ru.romangr.catbot.telegram.model.Chat

@StaticCommand(BotCommand.STATISTICS)
class StatisticsCommandHandler(private val actionFactory: TelegramActionFactory,
                               private val adminChatId: Long?,
                               statisticService: StatisticService)
    : StaticCommandHandler(statisticService) {

    override fun handleStringCommand(chat: Chat, text: String): List<TelegramAction> {
        if (isMessageNotFromAdmin(this.adminChatId, chat)) {
            return emptyList()
        }
        val (sinceDate, stats) = statisticService.commandStatistics()
        val messageTextBuilder = StringBuilder("Action statistics since $sinceDate:")
        stats.forEach { (action, value) -> messageTextBuilder.append("\n  $action: $value") }
        return listOf(actionFactory.newSendMessageAction(chat, messageTextBuilder.toString()))
    }
}
