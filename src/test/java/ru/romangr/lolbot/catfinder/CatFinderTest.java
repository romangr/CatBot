package ru.romangr.lolbot.catfinder;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import ru.romangr.lolbot.catfinder.Model.Cat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Roman 01.04.2017.
 */
class CatFinderTest {

  @Test
  void getCat() {
    Cat cat = new CatFinder(new RestTemplate()).getCat();

    assertNotNull(cat.getUrl());
    assertThat(cat.getUrl()).startsWith("http");
  }
}