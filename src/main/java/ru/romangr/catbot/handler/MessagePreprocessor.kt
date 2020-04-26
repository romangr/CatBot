package ru.romangr.catbot.handler

import ru.romangr.catbot.telegram.model.Message

class MessagePreprocessor(private val botName: String) {

    fun process(message: Message): Message {
        if (message.text == null) {
            return message
        }
        val processedText = message.text.replace(botName, "").trim()
        return Message(message.id, message.from, message.chat, processedText, message.video)
    }

}
