package ru.romangr.catbot.telegram.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TextMessageToSend(
    @SerialName("chat_id") val chatId: Long,
    val text: String,
) {
    constructor(chat: Chat, text: String) : this(chat.id, text)
}

@Serializable
data class DocumentMessageToSend(
    @SerialName("chat_id") val chatId: Long,
    @SerialName("document") val documentId: String,
)

@Serializable
data class VideoMessageToSend(
    @SerialName("chat_id") val chatId: Long,
    @SerialName("video") val videoId: String,
)

@Serializable
data class PhotoMessageToSend(
    @SerialName("chat_id") val chatId: Long,
    @SerialName("photo") val photoId: String,
)
