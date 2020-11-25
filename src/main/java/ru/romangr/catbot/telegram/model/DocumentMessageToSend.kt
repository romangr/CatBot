package ru.romangr.catbot.telegram.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DocumentMessageToSend(
        @JsonProperty("chat_id") val chatId: Long,
        @JsonProperty("document") val documentId: String
)
