package ru.romangr.catbot.handler

class MessagePreprocessor(private val botName: String) {

    fun process(messageText: String): String {
        return messageText.replace(botName, "").trim()
    }

}
