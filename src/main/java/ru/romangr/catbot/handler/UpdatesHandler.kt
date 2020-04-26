package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.TelegramActionExecutor
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.Update
import java.util.stream.Collectors

class UpdatesHandler(private val messagePreprocessor: MessagePreprocessor,
                     private val commandHandlers: List<CommandHandler>,
                     private val unknownCommandHandler: UnknownCommandHandler,
                     private val actionExecutor: TelegramActionExecutor) {

    fun handleUpdate(update: Update) {
        val chat = getChatFromUpdate(update)
        val preprocessedMessage = messagePreprocessor.process(update.message)
        val results = commandHandlers.stream()
                .map { commandHandler -> commandHandler.handle(chat, preprocessedMessage) }
                .collect(Collectors.toList())
        results.stream()
                .filter { it.isException }
                .forEach { e -> e.ifException { ex -> log.warn("Error during updates handling", ex) } }
        val notHandledAtAll = results.stream()
                .map { handlingResultExceptional -> handlingResultExceptional.map { it.status } }
                .noneMatch { e -> e.isException || e.value == HandlingStatus.HANDLED }
        if (notHandledAtAll) {
            log.debug("Unknown message type: {}", update.message)
            unknownCommandHandler.handle(chat)
                    .ifValue { handlingResult -> actionExecutor.execute(handlingResult.actions) }
        }
        val actions = results.stream()
                .filter { it.isValuePresent }
                .map { it.value }
                .flatMap { handlingResult -> handlingResult.actions.stream() }
                .collect(Collectors.toList())
        actionExecutor.execute(actions)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(UpdatesHandler::class.java)
        private fun getChatFromUpdate(update: Update): Chat {
            return update.message.chat
        }
    }
}
