package ru.romangr.catbot.subscription

import ru.romangr.catbot.catfinder.CatFinder
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.telegram.TelegramAdminNotifier
import ru.romangr.catbot.telegram.TelegramRequestExecutor
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.exceptional.Exceptional
import java.util.ArrayList
import java.util.LinkedList
import java.util.Objects
import java.util.Optional
import java.util.function.Function
import java.util.stream.Stream

class SubscribersService(private val subscribersRepository: SubscribersRepository,
                         private val requestExecutor: TelegramRequestExecutor,
                         private val catFinder: CatFinder,
                         private val actionFactory: TelegramActionFactory,
                         private val notifier: TelegramAdminNotifier) {
    private val messagesToSubscribers = LinkedList<String>()

    val subscribersCount: Int
        get() = subscribersRepository.subscribersCount

    val messageQueueLength: Int
        get() = messagesToSubscribers.size

    fun sendMessageToSubscribers(): Exceptional<List<TelegramAction>> {
        if (!requestExecutor.isConnectedToInternet) {
            log.warn(ISSUES_DURING_SENDING_MESSAGE)
            return Exceptional.exceptional(
                    RuntimeException(ISSUES_DURING_SENDING_MESSAGE)
            )
        }

        val messageFromQueue = messagesToSubscribers.poll()
        if (messageFromQueue != null) {
            log.info("Message from queue will be sent. There are {} posts left in the queue",
                    messagesToSubscribers.size)
            return getActionsForAllSubscribers(messageFromQueue, null)
        }

        return catFinder.cat
                .ifException { e -> log.warn("Exception getting cat", e) }
                .map { it.url }
                .flatMap {
                    getActionsForAllSubscribers(it, Function { chat ->
                        val messageToSubscriber = StringBuilder().append("Your daily cat")
                        Stream.of(chat.title, chat.firstName, chat.lastName, chat.username)
                                .filter { Objects.nonNull(it) }
                                .findFirst()
                                .ifPresent { identifier ->
                                    messageToSubscriber.append(", ")
                                            .append(identifier)
                                            .append("!")
                                }
                        messageToSubscriber.append("\n").toString()
                    })
                }
                .ifValue { log.info("Cat will be sent to {} subscribers", it.size) }
    }

    fun addMessageToSubscribers(message: String) {
        messagesToSubscribers.add(message)
    }

    fun deleteSubscriber(subscriber: Chat): Boolean {
        Exceptional.getExceptional { notifier.unsubscribed(subscriber) }
                .ifException { log.warn("Error trying to notify the admin", it) }
        return subscribersRepository.deleteSubscriber(subscriber)
    }

    fun addSubscriber(subscriber: Chat): Boolean {
        Exceptional.getExceptional { notifier.newSubscriber(subscriber) }
                .ifException { log.warn("Error trying to notify the admin", it) }
        return subscribersRepository.addSubscriber(subscriber)
    }

    private fun getActionsForAllSubscribers(message: String,
                                            prefixSupplier: Function<Chat, String>?)
            : Exceptional<List<TelegramAction>> {
        val actions = ArrayList<TelegramAction>(subscribersRepository.subscribersCount)
        for (chat in subscribersRepository.allSubscribers) {
            Optional.ofNullable(prefixSupplier)
                    .map { prefixSupplier!!.apply(chat) + message }
                    .or { Optional.of(message) }
                    .map<TelegramAction> { m -> actionFactory.newSendMessageAction(chat, m) }
                    .ifPresent { a -> actions.add(a) }
        }
        return Exceptional.exceptional(actions)
    }

    companion object {
        private const val ISSUES_DURING_SENDING_MESSAGE = "Can't send result to subscribers now, will send it later"
        private val log = org.slf4j.LoggerFactory.getLogger(SubscribersService::class.java)
    }
}
