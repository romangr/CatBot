package ru.romangr.lolbot.catfinder;

import ru.romangr.lolbot.catfinder.Model.Cat;
import org.junit.Test;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

/**
 * Roman 01.04.2017.
 */
public class CatFinderTest {

  @Test
  public void getCat() throws Exception {
    Cat cat = CatFinder.getCat();

    assertNotNull(cat.getUrl());
    assertThat(cat.getUrl(), startsWith("http"));
  }
}