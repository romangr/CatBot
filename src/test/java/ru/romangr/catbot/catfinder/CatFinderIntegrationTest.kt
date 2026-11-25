package ru.romangr.catbot.catfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.romangr.catbot.http.JsonHttpClient

/**
 * Roman 01.04.2017.
 */
internal class CatFinderIntegrationTest {

    private val catFinder = CatFinder(JsonHttpClient())

    @Test
    fun getCat() {
        val cat = catFinder.cat

        assertThat(cat.isValuePresent).describedAs { cat.exception.message }.isTrue()
        assertThat(cat.value.url).startsWith("http")
        println(cat.value.url)
    }
}
