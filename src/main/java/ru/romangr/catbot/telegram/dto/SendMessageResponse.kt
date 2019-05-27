package ru.romangr.catbot.telegram.dto

import ru.romangr.catbot.telegram.model.Message

class SendMessageResponse(val result: Message, val isOk: Boolean)
