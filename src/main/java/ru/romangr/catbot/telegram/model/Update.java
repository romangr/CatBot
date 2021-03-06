package ru.romangr.catbot.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

/**
 * Roman 27.10.2016.
 */
@Data
@Builder
@JsonDeserialize(builder = Update.UpdateBuilder.class)
public class Update {
    public int id;

    public Message message;

    @JsonProperty("editedMessage")
    public Message editedMessage;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UpdateBuilder {

        @JsonProperty("update_id")
        public Update.UpdateBuilder id(int id) {
            this.id = id;
            return this;
        }

    }
}
