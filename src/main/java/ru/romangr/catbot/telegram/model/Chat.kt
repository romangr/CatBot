package ru.romangr.catbot.telegram.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * Roman 27.10.2016.
 *
 * Also persisted in the subscribers file. Older versions wrote `firstName`/`lastName`,
 * so both spellings are accepted when reading.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Chat @JvmOverloads constructor(
    val id: Long,
    val title: String? = null,
    @SerialName("first_name") @JsonNames("firstName") val firstName: String? = null,
    @SerialName("last_name") @JsonNames("lastName") val lastName: String? = null,
    val username: String? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chat

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
