package ru.romangr.catbot.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.Message;
import ru.romangr.catbot.telegram.model.User;

class MessagePreprocessorTest {

  private MessagePreprocessor preprocessor = new MessagePreprocessor("@botName");

  @CsvSource({" a /test@botName test ,a /test test", "/test@botName,/test"})
  @ParameterizedTest
  void preprocessMessage(String text, String expectedResult) {
    Message message = messageWithText(text);
    assertThat(preprocessor.process(message).getText()).isEqualTo(expectedResult);
  }

  @NotNull
  private static Message messageWithText(String text) {
    Chat chat = new Chat(123123, "title", "first", "last", "username");
    User user = User.builder()
        .firstName("first")
        .lastName("last")
        .id(123123)
        .username("username")
        .build();
    return new Message(213, user, chat, text, null);
  }
}
