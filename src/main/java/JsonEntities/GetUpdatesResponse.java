package JsonEntities;

import Model.Update;
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
        return "JsonEntities.GetUpdatesResponse{" +
                "ok=" + ok +
                ", updates=" + updates +
                '}';
    }
}
