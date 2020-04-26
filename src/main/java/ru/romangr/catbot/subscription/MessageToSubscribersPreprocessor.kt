package ru.romangr.catbot.subscription

import ru.romangr.catbot.telegram.model.Chat
import java.util.*
import java.util.stream.Stream

enum class MessageToSubscribersTemplateVariable(val variable: String, val extractor: (Chat) -> String?) {
    IDENTIFIER("idf", { chat -> resolveUserIdentifier(chat) })
}

fun processTemplateVariables(chat: Chat, message: MessageToSubscribers): MessageToSubscribers {
    if (message.type != MessageToSubscribersType.TEXT) {
        return message
    }
    var processedMessage : String = message.text!!
    for (templateVariable in MessageToSubscribersTemplateVariable.values()) {
        val value = templateVariable.extractor(chat)
        value?.let { processedMessage = processedMessage.replace("$" + templateVariable.variable, it) }
    }
    return MessageToSubscribers.textMessage(processedMessage)
}

fun resolveUserIdentifier(chat: Chat): String? {
    return Stream.of(chat.title, chat.firstName, chat.lastName, chat.username)
            .filter { Objects.nonNull(it) }
            .findFirst()
            .orElse(null)
}
