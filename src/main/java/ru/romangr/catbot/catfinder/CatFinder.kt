package ru.romangr.catbot.catfinder

import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
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
        get() {
            var result: Exceptional<Cat> = Exceptional.empty()
            for (i in 1..RETRY_NUMBER) {
                result = retrieveCat()
                result.ifException { log.warn("Exception during cat retrieval", it) }
                if (result.isValuePresent) {
                    return result
                }
            }
            return result
        }

    private fun retrieveCat(): Exceptional<Cat> =
        Exceptional.getExceptional<ResponseEntity<Cat>> {
            restTemplate.exchange(requestEntity, object : ParameterizedTypeReference<Cat>() {

            })
        }
            .safelyMap {
                if (it.statusCode != HttpStatus.OK) {
                    val message = "Unexpected response from Cat API: ${it.statusCode}\n${it.headers}\n${it.body}"
                    throw RuntimeException(message)
                }
                return@safelyMap it.body ?: throw RuntimeException("Unexpected Cat response")
            }
            .map { Cat(UriComponentsBuilder.fromUriString(it.url).replaceQuery(null).toUriString()) }

    constructor(restTemplate: RestTemplate) {
        this.restTemplate = restTemplate
        val url = URI.create(prepareUrlWithoutApiKey().build())
        requestEntity = getRequestEntity(url)
    }

    constructor(restTemplate: RestTemplate, apiKey: String) {
        this.restTemplate = restTemplate
        val url = URI.create(prepareUrlWithoutApiKey().withParameter("api_key", apiKey).build())
        requestEntity = getRequestEntity(url)
    }


    private fun prepareUrlWithoutApiKey(): URLBuilder {
        return URLBuilder().withHost("https://cataas.com").withPath("cat")
            .withParameter("json", "true")
    }

    private fun getRequestEntity(url: URI): RequestEntity<Void> {
        return RequestEntity.get(url).accept(MediaType.APPLICATION_JSON).build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(CatFinder::class.java)
        private val SUPPORTED_CONTENT_TYPES: Array<MediaType> =
            arrayOf(MediaType.IMAGE_GIF, MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)
        private const val RETRY_NUMBER = 3
    }
}
