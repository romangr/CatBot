package ru.romangr.lolbot.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

/**
 * Roman 27.10.2016.
 */
@Data
@Builder
@Setter(AccessLevel.PACKAGE)
public class Message {
    @JsonProperty("message_id")
    private int id;

    private User from;

    private Chat chat;

    private String text;
}
