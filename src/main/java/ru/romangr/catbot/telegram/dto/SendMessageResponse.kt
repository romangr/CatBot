package ru.romangr.catbot.telegram.dto

import ru.romangr.catbot.telegram.model.Message

interface TelegramActionResponse<T> {
    val result: T
    val isOk: Boolean
}

class SendMessageResponse(override val result: Message, override val isOk: Boolean)
    : TelegramActionResponse<Message>

class SendVideoResponse(override val result: Message, override val isOk: Boolean)
    : TelegramActionResponse<Message>
