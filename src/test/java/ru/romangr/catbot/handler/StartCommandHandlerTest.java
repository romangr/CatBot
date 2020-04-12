package ru.romangr.catbot.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.jupiter.api.Test;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.exceptional.Exceptional;

class StartCommandHandlerTest {

  private TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
  private CommandHandler handler = new StartCommandHandler(actionFactory);

  @Test
  void handleCommandSuccessfully() {
    Chat chat = new Chat(1, null, null, null, null);
    given(actionFactory.newSendMessageAction(any(), any()))
        .willReturn(mock(TelegramAction.class));

    Exceptional<HandlingResult> result = handler.handle(chat, "/start");

    assertThat(result.isValuePresent()).isTrue();
    HandlingResult handlingResult = result.getValue();
    assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.HANDLED);
    assertThat(handlingResult.getActions()).hasSize(1);
    verify(actionFactory)
        .newSendMessageAction(chat,
            "Hi, let's see what cats we have for today!\n"
                + "/cat to get a random cat \uD83D\uDC31\n"
                + "/subscribe to get a random cat every day \uD83D\uDC08\n"
                + "If you like it — share this bot with your friends!");
    verifyNoMoreInteractions(actionFactory);
  }

  @Test
  void skipUnknownCommand() {
    Chat chat = new Chat(1, null, null, null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, "unknown");

    assertThat(result.isValuePresent()).isTrue();
    HandlingResult handlingResult = result.getValue();
    assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.SKIPPED);
    assertThat(handlingResult.getActions()).isEmpty();
    verifyZeroInteractions(actionFactory);
  }

  @Test
  void handleCommandWithException() {
    Chat chat = new Chat(1, null, null, null, null);
    given(actionFactory.newSendMessageAction(any(), any()))
        .willThrow(RuntimeException.class);

    Exceptional<HandlingResult> result = handler.handle(chat, "/start");

    assertThat(result.isException()).isTrue();
    result.ifException(e -> assertThat(e).isExactlyInstanceOf(RuntimeException.class));
    verify(actionFactory).newSendMessageAction(eq(chat), anyString());
  }

}
