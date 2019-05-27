package ru.romangr.catbot.telegram.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
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

    @JsonSetter(value = "firstName", nulls = Nulls.SKIP)
    fun setFirstNameCamel(lastName: String?) {
        this.lastName = lastName
    }

    @JsonSetter(value = "lastName", nulls = Nulls.SKIP)
    fun setLastNameCamel(lastName: String?) {
        this.lastName = lastName
    }

    constructor(id: Int) {
        this.id = id
    }

    constructor()
}
