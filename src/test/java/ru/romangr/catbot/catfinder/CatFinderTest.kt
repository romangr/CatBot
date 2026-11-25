package ru.romangr.catbot.catfinder

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.romangr.catbot.http.HttpStatusException
import ru.romangr.catbot.http.JsonHttpClient
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue

internal class CatFinderTest {

    private lateinit var server: HttpServer
    private val responses = ConcurrentLinkedQueue<(HttpExchange) -> Unit>()
    private val requests = ConcurrentLinkedQueue<String>()
    private lateinit var baseUrl: String

    @BeforeEach
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            exchange.use {
                requests.add(it.requestURI.toString())
                val handler = responses.poll() ?: respond(500, "no response stubbed")
                handler(it)
            }
        }
        server.start()
        baseUrl = "http://127.0.0.1:${server.address.port}"
    }

    @AfterEach
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun getCatSuccessfully() {
        responses.add(respond(200, """{"url": "https://cataas.com/cat/abc?position=center", "id": "abc"}"""))

        val cat = CatFinder(JsonHttpClient(), "secret", baseUrl).cat

        assertThat(cat.isValuePresent).describedAs { cat.exception?.message }.isTrue()
        assertThat(cat.value.url).isEqualTo("https://cataas.com/cat/abc")
        assertThat(requests).containsExactly("/cat?json=true&api_key=secret")
    }

    @Test
    fun getCatWithoutApiKey() {
        responses.add(respond(200, """{"url": "https://cataas.com/cat/abc"}"""))

        val cat = CatFinder(JsonHttpClient(), null, baseUrl).cat

        assertThat(cat.value.url).isEqualTo("https://cataas.com/cat/abc")
        assertThat(requests).containsExactly("/cat?json=true")
    }

    @Test
    fun getCatSuccessfullyAfterRetryOnServerError() {
        responses.add(respond(503, "unavailable"))
        responses.add(respond(200, """{"url": "https://cataas.com/cat/second"}"""))

        val cat = CatFinder(JsonHttpClient(), null, baseUrl).cat

        assertThat(cat.value.url).isEqualTo("https://cataas.com/cat/second")
        assertThat(requests).hasSize(2)
    }

    @Test
    fun getCatFailsAfterAllRetries() {
        repeat(3) { responses.add(respond(500, "boom")) }

        val cat = CatFinder(JsonHttpClient(), null, baseUrl).cat

        assertThat(cat.isException).isTrue()
        assertThat(cat.exception).isInstanceOf(HttpStatusException::class.java)
        assertThat((cat.exception as HttpStatusException).statusCode).isEqualTo(500)
        assertThat(requests).hasSize(3)
    }

    @Test
    fun removeQueryKeepsFragment() {
        assertThat(CatFinder.removeQuery("https://a.b/c?x=1#frag")).isEqualTo("https://a.b/c#frag")
        assertThat(CatFinder.removeQuery("https://a.b/c")).isEqualTo("https://a.b/c")
    }

    private fun respond(status: Int, body: String): (HttpExchange) -> Unit = { exchange ->
        val bytes = body.toByteArray()
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.write(bytes)
    }
}
