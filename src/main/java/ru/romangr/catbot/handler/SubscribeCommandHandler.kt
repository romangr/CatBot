package ru.romangr.catbot.handler

import org.slf4j.LoggerFactory
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.statistic.StatisticService
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat

@StaticCommand(BotCommand.SUBSCRIBE)
class SubscribeCommandHandler(private val actionFactory: TelegramActionFactory,
                              private val subscribersService: SubscribersService,
                              statisticService: StatisticService)
    : StaticCommandHandler(statisticService) {

    private val log = LoggerFactory.getLogger(CatCommandHandler::class.java)

    override fun handleStringCommand(chat: Chat, text: String): List<TelegramAction> =
            subscribersService.addSubscriber(chat)
                    .ifException { log.warn("Error adding subscriber", it) }
                    .map {
                        if (it) {
                            log.info("New subscriber. Total subscribers: {}.",
                                    subscribersService.subscribersCount)
                            MESSAGE_TO_NEW_SUBSCRIBER
                        } else {
                            YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE
                        }
                    }
                    .resumeOnException { ERROR_MESSAGE }
                    .map { listOf(actionFactory.newSendMessageAction(chat, it)) }
                    .value

    companion object {
        private const val MESSAGE_TO_NEW_SUBSCRIBER = "Thank you for subscription!"
        private const val YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE = "You have already subscribed!"
        private const val ERROR_MESSAGE = "Sorry, some error occurred when we tried to subscribe you, please try later";
    }

}
