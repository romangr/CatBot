package ru.romangr.catbot.telegram.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.romangr.catbot.telegram.dto.Document
import ru.romangr.catbot.telegram.dto.Video

/**
 * Roman 27.10.2016.
 */
@Serializable
data class Message(
    @SerialName("message_id") val id: Long,
    val from: User,
    val chat: Chat,
    val text: String? = null,
    val video: Video? = null,
    val document: Document? = null,
    val photo: List<PhotoSize>? = null,
)

@Serializable
data class PhotoSize(
    @SerialName("file_id") val fileId: String,
    val width: Long,
)
