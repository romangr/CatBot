package ru.romangr.catbot.telegram.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Roman 27.10.2016.
 */
@Serializable
data class Update @JvmOverloads constructor(
    @SerialName("update_id") val id: Long,
    val message: Message? = null,
    @SerialName("edited_message") val editedMessage: Message? = null,
)
