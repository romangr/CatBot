package ru.romangr.catbot.telegram.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

/**
 * Roman 27.10.2016.
 */
class Chat : Serializable {
    var id: Int? = null
    var title: String? = null

    @JsonProperty("first_name")
    var firstName: String? = null

    @JsonProperty("last_name")
    var lastName: String? = null

    var username: String? = null

    constructor(id: Int) {
        this.id = id
    }

    constructor()
}
