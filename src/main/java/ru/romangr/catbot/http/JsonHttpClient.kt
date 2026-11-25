package ru.romangr.catbot.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Minimal JSON-over-HTTP client built on top of [java.net.http.HttpClient].
 *
 * Any non-2xx response is reported as [HttpStatusException];
 * network failures surface as [java.io.IOException].
 */
class JsonHttpClient @JvmOverloads constructor(
    private val client: HttpClient = defaultHttpClient(),
    private val objectMapper: ObjectMapper = defaultObjectMapper(),
    private val requestTimeout: Duration = Duration.ofSeconds(60),
) {

    @Throws(IOException::class, InterruptedException::class)
    fun <T> get(url: String, responseType: Class<T>): T =
        get(URI.create(url), responseType)

    @JvmOverloads
    @Throws(IOException::class, InterruptedException::class)
    fun <T> get(url: URI, responseType: Class<T>, headers: Map<String, String> = emptyMap()): T {
        val builder = HttpRequest.newBuilder(url).GET()
        headers.forEach(builder::header)
        return send(builder, responseType)
    }

    @Throws(IOException::class, InterruptedException::class)
    fun <T> post(url: String, body: Any, responseType: Class<T>): T {
        val builder = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", APPLICATION_JSON)
            .POST(HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(body)))
        return send(builder, responseType)
    }

    private fun <T> send(builder: HttpRequest.Builder, responseType: Class<T>): T {
        val request = builder
            .header("Accept", APPLICATION_JSON)
            .timeout(requestTimeout)
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        val body = response.body() ?: ByteArray(0)
        if (response.statusCode() !in 200..299) {
            // The URI is deliberately not included: Telegram URLs contain the bot token.
            throw HttpStatusException(request.method(), response.statusCode(), String(body, Charsets.UTF_8))
        }
        return objectMapper.readValue(body, responseType)
    }

    companion object {
        const val APPLICATION_JSON = "application/json"

        @JvmStatic
        fun defaultHttpClient(): HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        /** Mirrors the lenient defaults Spring's RestTemplate used. */
        @JvmStatic
        fun defaultObjectMapper(): ObjectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}

class HttpStatusException(
    val method: String,
    val statusCode: Int,
    val responseBody: String,
) : RuntimeException("$method request failed with HTTP $statusCode: $responseBody") {

    val isClientError: Boolean get() = statusCode in 400..499
}
