package ru.romangr.catbot.subscription

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.romangr.catbot.telegram.model.Chat
import java.nio.file.Files
import java.nio.file.Path

internal class SubscribersFileFormatTest {

    @TempDir
    lateinit var dir: Path

    @Test
    fun readsFileWrittenByJacksonVersion() {
        val file = dir.resolve("subscribers.json")
        // Exact output of the previous (Jackson-based) SubscribersRepository.
        Files.writeString(file, """[{"id":5,"title":"t","username":"un","firstName":"fn","lastName":"ln"}]""")

        val chat = SubscribersRepository(file).allSubscribers.single()

        assertThat(chat.id).isEqualTo(5)
        assertThat(chat.title).isEqualTo("t")
        assertThat(chat.firstName).isEqualTo("fn")
        assertThat(chat.lastName).isEqualTo("ln")
        assertThat(chat.username).isEqualTo("un")
    }

    @Test
    fun writesSnakeCaseAndReadsItBack() {
        val file = dir.resolve("subscribers.json")
        SubscribersRepository(file).addSubscriber(Chat(7, null, "fn", "ln", null))

        assertThat(Files.readString(file)).contains("\"first_name\":\"fn\"").contains("\"last_name\":\"ln\"")
        val chat = SubscribersRepository(file).allSubscribers.single()
        assertThat(chat.firstName).isEqualTo("fn")
        assertThat(chat.lastName).isEqualTo("ln")
    }
}
