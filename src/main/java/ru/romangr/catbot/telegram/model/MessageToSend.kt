package ru.romangr.catbot.telegram.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class DocumentMessageToSend(
    @JsonProperty("chat_id") val chatId: Long,
    @JsonProperty("document") val documentId: String
)

data class VideoMessageToSend(
    @JsonProperty("chat_id") val chatId: Long,
    @JsonProperty("video") val videoId: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PhotoMessageToSend(
    @JsonProperty("chat_id") val chatId: Long,
    @JsonProperty("photo") val photoId: String,
    @JsonProperty("caption") val caption: String? = null
)
