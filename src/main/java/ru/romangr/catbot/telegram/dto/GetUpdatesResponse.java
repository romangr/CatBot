package ru.romangr.catbot.telegram.dto;

import ru.romangr.catbot.telegram.model.Update;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Roman 27.10.2016.
 */
public class GetUpdatesResponse {
    private boolean ok;

    @JsonProperty("result")
    private List<Update> updates;

    public boolean isOk() {
        return ok;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    @Override
    public String toString() {
        return "GetUpdatesResponse{" +
                "ok=" + ok +
                ", updates=" + updates +
                '}';
    }
}
