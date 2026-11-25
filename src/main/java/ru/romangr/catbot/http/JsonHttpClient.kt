package ru.romangr.catbot.http

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Minimal JSON-over-HTTP client built on top of [java.net.http.HttpClient] and kotlinx.serialization.
 *
 * Any non-2xx response is reported as [HttpStatusException];
 * network failures surface as [java.io.IOException].
 */
class JsonHttpClient @JvmOverloads constructor(
    private val client: HttpClient = defaultHttpClient(),
    private val json: Json = defaultJson,
    private val requestTimeout: Duration = Duration.ofSeconds(60),
) {

    @Throws(IOException::class, InterruptedException::class)
    fun <T> get(url: String, deserializer: DeserializationStrategy<T>): T =
        get(URI.create(url), deserializer)

    @Throws(IOException::class, InterruptedException::class)
    fun <T> get(url: URI, deserializer: DeserializationStrategy<T>): T =
        send(HttpRequest.newBuilder(url).GET(), deserializer)

    @Throws(IOException::class, InterruptedException::class)
    fun <B, T> post(
        url: String,
        body: B,
        serializer: SerializationStrategy<B>,
        deserializer: DeserializationStrategy<T>,
    ): T {
        val builder = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", APPLICATION_JSON)
            .POST(HttpRequest.BodyPublishers.ofString(json.encodeToString(serializer, body)))
        return send(builder, deserializer)
    }

    private fun <T> send(builder: HttpRequest.Builder, deserializer: DeserializationStrategy<T>): T {
        val request = builder
            .header("Accept", APPLICATION_JSON)
            .timeout(requestTimeout)
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val body = response.body() ?: ""
        if (response.statusCode() !in 200..299) {
            // The URI is deliberately not included: Telegram URLs contain the bot token.
            throw HttpStatusException(request.method(), response.statusCode(), body)
        }
        return json.decodeFromString(deserializer, body)
    }

    companion object {
        const val APPLICATION_JSON = "application/json"

        /** Lenient: external APIs add fields over time. */
        @JvmField
        val defaultJson: Json = Json { ignoreUnknownKeys = true }

        @JvmStatic
        fun defaultHttpClient(): HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }
}

class HttpStatusException(
    val method: String,
    val statusCode: Int,
    val responseBody: String,
) : RuntimeException("$method request failed with HTTP $statusCode: $responseBody") {

    val isClientError: Boolean get() = statusCode in 400..499
}
