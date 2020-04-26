package ru.romangr.catbot.telegram.model

import com.fasterxml.jackson.annotation.JsonProperty

data class VideoMessageToSend(
        @JsonProperty("chat_id") val chatId: Int,
        @JsonProperty("video") val videoId: String
)
