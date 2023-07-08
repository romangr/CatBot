package ru.romangr.catbot.subscription

import ru.romangr.catbot.catfinder.CatFinder
import ru.romangr.catbot.delayed.DelayedMessageRepository
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.telegram.TelegramAdminNotifier
import ru.romangr.catbot.telegram.TelegramRequestExecutor
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.exceptional.Exceptional
import java.util.ArrayList

class SubscribersService(private val subscribersRepository: SubscribersRepository,
                         private val requestExecutor: TelegramRequestExecutor,
                         private val catFinder: CatFinder,
                         private val actionFactory: TelegramActionFactory,
                         private val notifier: TelegramAdminNotifier,
                         private val delayedMessageRepository: DelayedMessageRepository) {

  val subscribersCount: Int
    get() = subscribersRepository.subscribersCount

  val messageQueueLength: Int
    get() = delayedMessageRepository.count()

  fun sendMessageToSubscribers(): Exceptional<List<TelegramAction>> {
    if (!requestExecutor.isConnectedToInternet) {
      log.warn(ISSUES_DURING_SENDING_MESSAGE)
      return Exceptional.exceptional(
          RuntimeException(ISSUES_DURING_SENDING_MESSAGE)
      )
    }

    val messageFromQueue = delayedMessageRepository.nextMessage()
    if (messageFromQueue != null) {
      log.info("Message from queue will be sent. There are {} posts left in the queue",
          delayedMessageRepository.count())
      return getActionsForAllSubscribers(messageFromQueue) { chat, message ->
        processTemplateVariables(chat, message)
      }
    }

    return catFinder.cat
        .ifException { e -> log.warn("Exception getting cat", e) }
        .map { MessageToSubscribers.textMessage(it.url) }
        .flatMap {
          getActionsForAllSubscribers(it) { chat, message ->
            if (message.type != MessageToSubscribersType.TEXT) {
              return@getActionsForAllSubscribers message
            }
            val messageToSubscriber = StringBuilder().append("Your daily cat")
            resolveUserIdentifier(chat)?.let { identifier ->
              messageToSubscriber.append(", ")
                  .append(identifier)
                  .append("!")
            }
            messageToSubscriber.append("\n").append(message.text).toString()
            MessageToSubscribers.textMessage(messageToSubscriber.toString())
          }
        }
        .ifValue { log.info("Cat will be sent to {} subscribers", it.size) }
  }

  fun addMessageToSubscribers(message: MessageToSubscribers) {
    delayedMessageRepository.add(message)
  }

  fun deleteSubscriber(subscriber: Chat): Boolean {
    Exceptional.getExceptional { notifier.unsubscribed(subscriber) }
        .ifException { log.warn("Error trying to notify the admin", it) }
    return subscribersRepository.deleteSubscriber(subscriber)
  }

  fun addSubscriber(subscriber: Chat): Exceptional<Boolean> =
      Exceptional.getExceptional { subscribersRepository.addSubscriber(subscriber) }
          .ifValue { isAdded ->
            if (isAdded) {
              Exceptional.getExceptional { notifier.newSubscriber(subscriber) }
                  .ifException { log.warn("Error trying to notify the admin", it) }
            }
          }


  private fun getActionsForAllSubscribers(message: MessageToSubscribers,
                                          messagePreprocessor: (Chat, MessageToSubscribers) ->
                                          MessageToSubscribers)
      : Exceptional<List<TelegramAction>> {
    val actions = ArrayList<TelegramAction>(subscribersRepository.subscribersCount)
    for (chat in subscribersRepository.allSubscribers) {
      val action = actionFactory.newAction(chat, messagePreprocessor(chat, message)) {
          when (it) {
              ExecutionResult.BOT_IS_BLOCKED_BY_USER -> subscribersRepository.deleteSubscriber(chat)
              ExecutionResult.USER_IS_DEACTIVATED -> subscribersRepository.deleteSubscriber(chat)
              ExecutionResult.BOT_KICKED_FROM_GROUP_CHAT -> subscribersRepository.deleteSubscriber(chat)
              ExecutionResult.CHAT_NOT_FOUND -> subscribersRepository.deleteSubscriber(chat)
              else -> {}
          }
      }
      actions.add(action)
    }
    return Exceptional.exceptional(actions)
  }

  companion object {
    private const val ISSUES_DURING_SENDING_MESSAGE = "Can't send result to subscribers now, will send it later"
    private val log = org.slf4j.LoggerFactory.getLogger(SubscribersService::class.java)
  }
}
