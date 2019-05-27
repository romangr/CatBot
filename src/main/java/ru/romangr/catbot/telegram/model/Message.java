package ru.romangr.catbot.telegram.model;

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
@JsonDeserialize(builder = Message.MessageBuilder.class)
public class Message {
    @JsonProperty("message_id")
    public int id;

    public User from;

    public Chat chat;

    public String text;

    @JsonPOJOBuilder(withPrefix = "")
    public static class MessageBuilder {

        @JsonProperty("message_id")
        public MessageBuilder id(int id) {
            this.id = id;
            return this;
        }

    }
}
