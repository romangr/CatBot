package ru.romangr.catbot.catfinder

import lombok.extern.slf4j.Slf4j
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import ru.romangr.catbot.utils.URLBuilder
import ru.romangr.exceptional.Exceptional
import java.net.URI

/**
 * Roman 01.04.2017.
 */
@Slf4j
class CatFinder {

    private val restTemplate: RestTemplate
    private val requestEntity: RequestEntity<Void>

    val cat: Exceptional<Cat>
        get() = Exceptional
                .getExceptional<ResponseEntity<List<Cat>>> {
                    restTemplate.exchange(requestEntity,
                            object : ParameterizedTypeReference<List<Cat>>() {

                            })
                }
                .safelyMap {
                    if (it.statusCode != HttpStatus.OK) {
                        val message = "Unexpected response from Cat API: ${it.statusCode}\n${it.headers}\n${it.body}"
                        throw RuntimeException(message)
                    }
                    return@safelyMap it
                }
                .map { it.body }
                .map { list -> list?.get(0) }

    constructor(restTemplate: RestTemplate) {
        this.restTemplate = restTemplate
        val url = URI.create(prepareUrlWithoutApiKey().build())
        requestEntity = getRequestEntity(url)
    }

    constructor(restTemplate: RestTemplate, apiKey: String) {
        this.restTemplate = restTemplate
        val url = URI.create(prepareUrlWithoutApiKey()
                .withParameter("api_key", apiKey)
                .build())
        requestEntity = getRequestEntity(url)
    }


    private fun prepareUrlWithoutApiKey(): URLBuilder {
        return URLBuilder()
                .withHost("https://api.thecatapi.com")
                .withPath("api/images/get")
                .withParameter("format", "json")
    }

    private fun getRequestEntity(url: URI): RequestEntity<Void> {
        return RequestEntity.get(url)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .build()
    }
}
