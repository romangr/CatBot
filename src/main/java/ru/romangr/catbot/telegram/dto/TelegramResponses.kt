package ru.romangr.catbot.telegram.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.romangr.catbot.telegram.model.Update

/** Generic Telegram Bot API envelope: `{"ok": true, "result": ...}`. */
@Serializable
data class TelegramResponse<T>(
    @SerialName("ok") val isOk: Boolean,
    val result: T? = null,
)

@Serializable
data class Bot(
    val id: Long,
    @SerialName("first_name") val firstName: String? = null,
    val username: String? = null,
)

typealias GetUpdatesResponse = TelegramResponse<List<Update>>
typealias GetMeResponse = TelegramResponse<Bot>
