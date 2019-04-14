package ru.romangr.lolbot.telegram.model;

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

    private String firstName;

    private String lastName;

    private String username;

    public Chat(int id) {
        this.id = id;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ChatBuilder {}

}
