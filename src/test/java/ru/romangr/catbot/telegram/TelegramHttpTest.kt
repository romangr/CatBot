package ru.romangr.catbot.telegram

import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.http.HttpStatusException
import ru.romangr.catbot.http.JsonHttpClient
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.ExecutionResult
import java.net.InetSocketAddress

/**
 * Exercises the real HTTP + JSON path against an in-process server.
 */
internal class TelegramHttpTest {

    private lateinit var server: HttpServer
    private lateinit var baseUrl: String
    @Volatile private var status = 200
    @Volatile private var body = ""
    @Volatile private var lastRequestBody = ""
    @Volatile private var lastRequestPath = ""

    @BeforeEach
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            exchange.use {
                lastRequestPath = it.requestURI.toString()
                lastRequestBody = String(it.requestBody.readAllBytes())
                val bytes = body.toByteArray()
                it.responseHeaders.add("Content-Type", "application/json")
                it.sendResponseHeaders(status, bytes.size.toLong())
                it.responseBody.write(bytes)
            }
        }
        server.start()
        baseUrl = "http://127.0.0.1:${server.address.port}/botTOKEN"
    }

    @AfterEach
    fun tearDown() = server.stop(0)

    @Test
    fun getUpdatesParsesResponseWithUnknownFields() {
        body = """
            {"ok": true, "result": [{
              "update_id": 42,
              "message": {
                "message_id": 7, "date": 1700000000,
                "from": {"id": 1, "is_bot": false, "first_name": "A", "language_code": "en"},
                "chat": {"id": 100, "type": "private", "first_name": "A"},
                "text": "/cat"
              }
            }]}
        """.trimIndent()

        val updates = TelegramRequestExecutor(JsonHttpClient(), baseUrl).getUpdates(5)

        assertThat(lastRequestPath).isEqualTo("/botTOKEN/getUpdates?offset=5")
        assertThat(updates.value).hasSize(1)
        assertThat(updates.value[0].id).isEqualTo(42)
        assertThat(updates.value[0].message.chat.id).isEqualTo(100)
        assertThat(updates.value[0].message.text).isEqualTo("/cat")
    }

    @Test
    fun getUpdatesReportsTooManyRequests() {
        status = 429
        body = """{"ok": false, "error_code": 429, "description": "Too Many Requests"}"""

        val updates = TelegramRequestExecutor(JsonHttpClient(), baseUrl).getUpdates(0)

        assertThat(updates.exception).isInstanceOf(HttpStatusException::class.java)
        assertThat((updates.exception as HttpStatusException).statusCode).isEqualTo(429)
        assertThat(updates.exception.message).doesNotContain("TOKEN")
    }

    @Test
    fun isConnectedToInternetReturnsFalseWhenServerIsDown() {
        server.stop(0)

        assertThat(TelegramRequestExecutor(JsonHttpClient(), baseUrl).isConnectedToInternet).isFalse()
    }

    @Test
    fun sendMessagePostsJsonAndSucceeds() {
        body = """
            {"ok": true, "result": {"message_id": 8, "date": 1,
              "from": {"id": 2, "is_bot": true, "first_name": "Bot"},
              "chat": {"id": 100, "type": "private"}, "text": "hi"}}
        """.trimIndent()

        val result = TelegramActionFactory(JsonHttpClient(), baseUrl)
            .newSendMessageAction(Chat(100), "hi").execute()

        assertThat(result.value).isEqualTo(ExecutionResult.SUCCESS)
        assertThat(lastRequestPath).isEqualTo("/botTOKEN/sendMessage")
        assertThat(lastRequestBody).contains("\"chat_id\":100").contains("\"text\":\"hi\"")
    }

    @Test
    fun sendMessageMapsTelegramErrors() {
        val cases = listOf(
            Triple(429, "Too Many Requests: retry after 5", ExecutionResult.RATE_LIMIT_FAILURE),
            Triple(403, "Forbidden: bot was blocked by the user", ExecutionResult.BOT_IS_BLOCKED_BY_USER),
            Triple(403, "Forbidden: user is deactivated", ExecutionResult.USER_IS_DEACTIVATED),
            Triple(403, "Forbidden: bot was kicked from the group chat", ExecutionResult.BOT_KICKED_FROM_GROUP_CHAT),
            Triple(400, "Bad Request: chat not found", ExecutionResult.CHAT_NOT_FOUND),
            Triple(500, "Internal Server Error", ExecutionResult.FAILURE),
        )
        for ((code, description, expected) in cases) {
            status = code
            body = """{"ok": false, "error_code": $code, "description": "$description"}"""
            var handled: ExecutionResult? = null

            val result = TelegramActionFactory(JsonHttpClient(), baseUrl)
                .newSendPhotoAction(Chat(100), "file-id") { handled = it }.execute()

            assertThat(result.value).describedAs(description).isEqualTo(expected)
            assertThat(handled).describedAs(description).isEqualTo(expected)
        }
    }
}
