package ru.romangr.catbot.telegram.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Video(
    @SerialName("file_id") val fileId: String,
)

@Serializable
data class Document(
    @SerialName("file_id") val fileId: String,
)
