package ru.romangr.lolbot.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Chat implements Serializable {
    private int id;
    private String title;
    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("username")
    private String username;

    public Chat(int id) {
        this.id = id;
    }

}
