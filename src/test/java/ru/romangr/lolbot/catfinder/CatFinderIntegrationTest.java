package ru.romangr.lolbot.catfinder;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.lolbot.catfinder.Model.Cat;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roman 01.04.2017.
 */
class CatFinderIntegrationTest {

    private RestTemplate restTemplate = new RestTemplate();
    private CatFinder catFinder = new CatFinder(restTemplate);

    @Test
    void getCat() {
        Exceptional<Cat> cat = catFinder.getCat();

        assertThat(cat.isValuePresent()).isTrue();
        assertThat(cat.getValue().getUrl()).startsWith("http");
        System.out.println(cat.getValue().getUrl());
    }
}