package ru.romangr.catbot.telegram

import ru.romangr.catbot.executor.TelegramActionExecutor
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.utils.PropertiesResolver
import java.util.Objects
import java.util.Optional
import java.util.function.Function
import java.util.stream.Collectors.joining
import java.util.stream.Stream

class TelegramAdminNotifier(private val actionFactory: TelegramActionFactory,
                            private val actionExecutor: TelegramActionExecutor,
                            propertiesResolver: PropertiesResolver,
                            private val adminChatId: Long?) {

    private val botStartedMessage = """
        |Bot started! ${propertiesResolver.buildInfo}
        |Updates check period: ${propertiesResolver.updatesCheckPeriod}
        |Time to send a message to subscribers: ${propertiesResolver.timeToSendMessageToSubscribers}"""
            .trimMargin()
    private val newSubscriberMessage = Function<String, String> { "New subscriber: $it" }
    private val unsubscribedMessage = Function<String, String> { "Unsubscribed: $it" }

    fun botStarted(subscribersCount: Int) {
        val subscribersInfo = "Number of subscribers: $subscribersCount"
        val message = botStartedMessage + '\n' + subscribersInfo
        notifyAdmin(Function { this.sendMessageAction(it, message) })
    }

    fun newSubscriber(subscriber: Chat) {
        notifyAdmin(Function { this.sendMessageAction(it, newSubscriberMessage.apply(getSubscriberUsername(subscriber))) })
    }

    fun unsubscribed(subscriber: Chat) {
        notifyAdmin(Function { this.sendMessageAction(it, unsubscribedMessage.apply(getSubscriberUsername(subscriber))) })
    }

    fun sendText(message: String) {
        notifyAdmin(Function { this.sendMessageAction(it, message) })
    }

    private fun getSubscriberUsername(subscriber: Chat) =
            Stream.of(subscriber.title, subscriber.firstName, subscriber.lastName, subscriber.username)
                    .filter { Objects.nonNull(it) }
                    .collect(joining(" | ", "[ ", " ]"))

    private fun notifyAdmin(actionProvider: Function<Long, TelegramAction>) {
        Optional.ofNullable(adminChatId)
                .map { actionProvider.apply(it) }
                .ifPresent { actionExecutor.execute(listOf(it)) }
    }

    private fun sendMessageAction(it: Long, message: String) =
            actionFactory.newSendMessageAction(Chat(it), message)
}
