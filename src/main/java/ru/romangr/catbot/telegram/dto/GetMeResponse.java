package ru.romangr.catbot.telegram.dto;

import ru.romangr.catbot.telegram.model.Bot;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Roman 27.10.2016.
 */
@Setter
@Getter
@ToString
public class GetMeResponse {
    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("result")
    private Result result;

    @Setter
    @Getter
    @ToString
    public class Result implements Bot {
        @JsonProperty("id")
        private int id;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("username")
        private String username;

        @Override
        public Bot getMe() {
            return this;
        }
    }

}
