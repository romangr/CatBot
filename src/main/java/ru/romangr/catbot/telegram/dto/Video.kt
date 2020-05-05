package ru.romangr.catbot.telegram.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Video(
        @JsonProperty("file_id") val fileId: String
)

data class Document(
        @JsonProperty("file_id") val fileId: String
)
