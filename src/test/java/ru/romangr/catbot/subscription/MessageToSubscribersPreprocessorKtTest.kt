package ru.romangr.catbot.subscription

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import ru.romangr.catbot.telegram.model.Chat
import java.util.stream.Stream

internal class MessageToSubscribersPreprocessorKtTest {

    @CsvSource(
            value = ["Mr.X_Hello \$idf, it's your message_Hello Mr.X, it's your message"],
            delimiter = '_'
    )
    @ParameterizedTest
    internal fun processTemplateVariablesForChats(chatName: String, template: String, expectedResult: String) {
        val chat = Chat(id = 1, firstName = chatName)

        val actualResult = processTemplateVariables(chat, template)

        assertThat(actualResult).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("resolveIdentifierData")
    internal fun resolveIdentifier(chat: Chat, resolvedIdentifier: String?) {
        assertThat(resolveUserIdentifier(chat)).isEqualTo(resolvedIdentifier)
    }

    companion object {

        @JvmStatic
        fun resolveIdentifierData(): Stream<Arguments> {
            return Stream.of(
                    Arguments.arguments(Chat(id = 1, title = "title", firstName = "first", lastName = "last", username = "username"), "title"),
                    Arguments.arguments(Chat(id = 1, title = null, firstName = "first", lastName = "last", username = "username"), "first"),
                    Arguments.arguments(Chat(id = 1, title = null, firstName = null, lastName = "last", username = "username"), "last"),
                    Arguments.arguments(Chat(id = 1, title = null, firstName = null, lastName = null, username = "username"), "username"),
                    Arguments.arguments(Chat(id = 1, title = null, firstName = null, lastName = null, username = null), null)
            )
        }
    }

}


