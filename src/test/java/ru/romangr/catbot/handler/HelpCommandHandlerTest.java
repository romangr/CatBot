package ru.romangr.catbot.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.statistic.StatisticService;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.Message;
import ru.romangr.catbot.telegram.model.User;
import ru.romangr.exceptional.Exceptional;

class HelpCommandHandlerTest {

  private TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
  private StatisticService statisticService = mock(StatisticService.class);
  private CommandHandler handler = new HelpCommandHandler(actionFactory, 17, statisticService);

  @Test
  void handleCommandSuccessfully() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    given(actionFactory.newSendMessageAction(any(), any()))
        .willReturn(mock(TelegramAction.class));
    Message message = new Message(4234, user, chat, "/help", null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isValuePresent()).isTrue();
    HandlingResult handlingResult = result.getValue();
    assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.HANDLED);
    assertThat(handlingResult.getActions()).hasSize(1);
    verify(actionFactory)
        .newSendMessageAction(chat, "/cat to get a random cat \uD83D\uDC31\n"
            + "/subscribe to get a random cat every day \uD83D\uDC08 (17:00 UTC)");
    verifyNoMoreInteractions(actionFactory);
  }

  @Test
  void skipUnknownCommand() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    Message message = new Message(4234, user, chat, "unknown", null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isValuePresent()).isTrue();
    HandlingResult handlingResult = result.getValue();
    assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.SKIPPED);
    assertThat(handlingResult.getActions()).isEmpty();
    verifyNoMoreInteractions(actionFactory);
  }

  @Test
  void handleCommandWithException() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    given(actionFactory.newSendMessageAction(any(), any()))
        .willThrow(RuntimeException.class);
    Message message = new Message(4234, user, chat, "/help", null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isException()).isTrue();
    result.ifException(e -> assertThat(e).isExactlyInstanceOf(RuntimeException.class));
    verify(actionFactory).newSendMessageAction(eq(chat), anyString());
  }

}
