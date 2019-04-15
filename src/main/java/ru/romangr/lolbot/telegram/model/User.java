package ru.romangr.lolbot.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

/**
 * Roman 27.10.2016.
 */
@Value
@Builder
@JsonDeserialize(builder = User.UserBuilder.class)
public class User {
    private int id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String username;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserBuilder {

        @JsonProperty("first_name")
        public User.UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        @JsonProperty("last_name")
        public User.UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

    }
}
