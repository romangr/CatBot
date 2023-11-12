package ru.romangr.catbot.telegram.model

import com.fasterxml.jackson.annotation.JsonProperty
import ru.romangr.catbot.telegram.dto.Document
import ru.romangr.catbot.telegram.dto.Video

/**
 * Roman 27.10.2016.
 */
data class Message(
        @JsonProperty("message_id") val id: Long,
        val from: User,
        val chat: Chat,
        val text: String? = null,
        val video: Video? = null,
        val document: Document? = null,
        val photo: List<PhotoSize>? = null,
        val caption: String? = null
)

data class PhotoSize(
        @JsonProperty("file_id") var fileId: String,
        var width: Long
)