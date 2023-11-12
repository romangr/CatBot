package ru.romangr.catbot.catfinder

import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
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
            Exceptional.getExceptional<ResponseEntity<List<Cat>>> {
                restTemplate.exchange(requestEntity, object : ParameterizedTypeReference<List<Cat>>() {

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
                    .safelyMap {
                        var realUrl = it!!.url
                        var response = restTemplate.exchange(realUrl, HttpMethod.HEAD, null, String::class.java)
                        if (response.statusCode === HttpStatus.MOVED_PERMANENTLY) {
                            val location = response.headers.location
                            if (location == null) {
                                throw RuntimeException("No location along with redirect response to ${it.url}");
                            }
                            response = restTemplate.exchange(location, HttpMethod.HEAD, null, String::class.java)
                            realUrl = location.toString()
                        }
                        val contentType = response.headers.contentType
                        if (contentType == null || !SUPPORTED_CONTENT_TYPES.contains(contentType)) {
                            throw RuntimeException("Unsupported content type: $contentType")
                        }
                        val contentLength = response.headers.contentLength
                        if (contentLength > MAX_PIC_SIZE_BYTES) {
                            throw RuntimeException("Content length too large: $contentLength")
                        }
                        return@safelyMap Cat(realUrl)
                    }

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
        return URLBuilder().withHost("https://api.thecatapi.com").withPath("api/images/get").withParameter("format", "json")
    }

    private fun getRequestEntity(url: URI): RequestEntity<Void> {
        return RequestEntity.get(url).accept(MediaType.APPLICATION_JSON_UTF8).build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(CatFinder::class.java)
        private val SUPPORTED_CONTENT_TYPES: List<MediaType> = listOf(MediaType.IMAGE_GIF, MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)
        private const val RETRY_NUMBER = 3
        private const val MAX_PIC_SIZE_BYTES = 10_000_000
    }
}
