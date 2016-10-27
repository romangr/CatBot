package JsonEntities;

import Model.Bot;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Roman 27.10.2016.
 */
public class GetMeResponse {
    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("result")
    private Result result;

    @Override
    public String toString() {
        return "JsonEntities.GetMeResponse{" +
                "ok=" + ok +
                ", result=" + result +
                '}';
    }

    public class Result implements Bot {
        @JsonProperty("id")
        private int id;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("username")
        private String username;

        @Override
        public String toString() {
            return "{" +
                    "id=" + id +
                    ", firstName='" + firstName + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }

        public int getId() {
            return id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getUsername() {
            return username;
        }
    }

    public boolean isOk() {
        return ok;
    }

    public Result getResult() {
        return result;
    }
}
