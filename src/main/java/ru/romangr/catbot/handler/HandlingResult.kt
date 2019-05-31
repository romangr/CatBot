package ru.romangr.catbot.handler

import ru.romangr.catbot.executor.action.TelegramAction

class HandlingResult(val actions: List<TelegramAction>, val status: HandlingStatus)
