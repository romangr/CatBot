package ru.romangr.catbot.handler

import lombok.RequiredArgsConstructor
import ru.romangr.catbot.catfinder.CatFinder
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.statistic.StatisticService
import ru.romangr.catbot.telegram.model.Chat

@StaticCommand(BotCommand.CAT)
@RequiredArgsConstructor
class CatCommandHandler(private val actionFactory: TelegramActionFactory,
                        private val catFinder: CatFinder,
                        statisticService: StatisticService)
    : StaticCommandHandler(statisticService) {

    override fun handleStringCommand(chat: Chat, text: String): List<TelegramAction> {
        return catFinder.cat
                .map { it.url }
                .ifException { e -> log.warn("/cat can't be handled", e) }
                .resumeOnException { ERROR_MESSAGE }
                .map { catUrl -> listOf((actionFactory.newSendMessageAction(chat, catUrl))) }
                .value
    }

    companion object {
        private const val ERROR_MESSAGE = "Sorry, your cat can't be delivered now, please try later"
        private val log = org.slf4j.LoggerFactory.getLogger(CatCommandHandler::class.java)
    }
}
