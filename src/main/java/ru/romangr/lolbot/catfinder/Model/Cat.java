package ru.romangr.lolbot.catfinder.Model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

/**
 * Roman 01.04.2017.
 */
@Value
@Builder
@JsonDeserialize(builder = Cat.CatBuilder.class)
public class Cat {

    private final String url;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CatBuilder {}
}
