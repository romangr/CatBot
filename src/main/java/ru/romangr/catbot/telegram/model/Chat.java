package ru.romangr.catbot.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.io.Serializable;

/**
 * Roman 27.10.2016.
 */
@Data
@AllArgsConstructor
@Builder
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@JsonDeserialize(builder = Chat.ChatBuilder.class)
public class Chat implements Serializable {
    private int id;
    private String title;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String username;

    public Chat(int id) {
        this.id = id;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ChatBuilder {

        @JsonProperty("first_name")
        public Chat.ChatBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        @JsonProperty("last_name")
        public Chat.ChatBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

    }

}
