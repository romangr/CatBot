package ru.romangr.lolbot.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Roman 27.10.2016.
 */
public class Message {
    @JsonProperty("message_id")
    private int id;

    private User from;

    private Chat chat;

    private String text;

    public int getId() {
        return id;
    }

    public User getFrom() {
        return from;
    }

    public Chat getChat() {
        return chat;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", from=" + from +
                ", text='" + text + '\'' +
                '}';
    }
}
