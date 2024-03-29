package ru.romangr.catbot.telegram.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Roman 27.10.2016.
 */
data class Chat(
        @JsonProperty("id") val id: Long,
        @JsonProperty("title") val title: String? = null,
        @JsonProperty("first_name") val firstName: String? = null,
        @JsonProperty("last_name") val lastName: String? = null,
        @JsonProperty("username") val username: String? = null
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
