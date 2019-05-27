package ru.romangr.catbot.catfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate

/**
 * Roman 01.04.2017.
 */
internal class CatFinderIntegrationTest {

    private val restTemplate = RestTemplate()
    private val catFinder = CatFinder(restTemplate)

    @Test
    fun getCat() {
        val cat = catFinder.cat

        assertThat(cat.isValuePresent).isTrue()
        assertThat(cat.value.url).startsWith("http")
        println(cat.value.url)
    }
}
