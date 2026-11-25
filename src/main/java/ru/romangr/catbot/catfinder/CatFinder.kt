package ru.romangr.catbot.catfinder

import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import ru.romangr.catbot.http.JsonHttpClient
import ru.romangr.catbot.utils.URLBuilder
import ru.romangr.exceptional.Exceptional
import java.net.URI

/**
 * Roman 01.04.2017.
 */
@Slf4j
class CatFinder {

    private val httpClient: JsonHttpClient
    private val url: URI

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
        Exceptional.getExceptional<Cat> { httpClient.get(url, Cat::class.java) }
            .map { Cat(removeQuery(it.url)) }

    @JvmOverloads
    constructor(httpClient: JsonHttpClient, apiKey: String? = null, baseUrl: String = DEFAULT_BASE_URL) {
        this.httpClient = httpClient
        val builder = URLBuilder().withHost(baseUrl).withPath("cat").withParameter("json", "true")
        if (apiKey != null) {
            builder.withParameter("api_key", apiKey)
        }
        url = URI.create(builder.build())
    }

    companion object {
        private val log = LoggerFactory.getLogger(CatFinder::class.java)
        private const val RETRY_NUMBER = 3
        private const val DEFAULT_BASE_URL = "https://cataas.com"

        internal fun removeQuery(url: String): String {
            val queryStart = url.indexOf('?')
            if (queryStart < 0) {
                return url
            }
            val fragmentStart = url.indexOf('#', queryStart)
            return url.substring(0, queryStart) + if (fragmentStart < 0) "" else url.substring(fragmentStart)
        }
    }
}
