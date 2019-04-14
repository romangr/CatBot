package ru.romangr.lolbot.handler;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MessagePreprocessorTest {

    private MessagePreprocessor preprocessor = new MessagePreprocessor("@botName");

    @CsvSource({" a /test@botName test ,a /test test", "/test@botName,/test"})
    @ParameterizedTest
    void preprocessMessage(String message, String expectedResult) {
        assertThat(preprocessor.process(message)).isEqualTo(expectedResult);
    }
}