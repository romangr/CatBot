package ru.romangr.lolbot.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Roman 27.10.2016.
 */
public class Update {
    @JsonProperty("update_id")
    private int id;

    private Message message;

    @JsonProperty("editedMessage")
    private Message editedMessage;

    public int getId() {
        return id;
    }

    public Message getMessage() {
        return message;
    }

    public Message getEditedMessage() {
        return editedMessage;
    }

    @Override
    public String toString() {
        return "Update{" +
                "update_id=" + id +
                ", result=" + message +
                ", edited_message=" + editedMessage +
                '}';
    }
}
