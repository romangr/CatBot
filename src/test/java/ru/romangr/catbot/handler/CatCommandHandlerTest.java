package ru.romangr.catbot.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static ru.romangr.exceptional.Exceptional.exceptional;

import org.junit.jupiter.api.Test;
import ru.romangr.catbot.catfinder.Cat;
import ru.romangr.catbot.catfinder.CatFinder;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.Message;
import ru.romangr.catbot.telegram.model.User;
import ru.romangr.exceptional.Exceptional;

class CatCommandHandlerTest {

  private TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
  private CatFinder catFinder = mock(CatFinder.class);
  private CommandHandler handler = new CatCommandHandler(actionFactory, catFinder);

  @Test
  void handleCommandSuccessfully() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    given(catFinder.getCat()).willReturn(exceptional(new Cat("url")));
    given(actionFactory.newSendMessageAction(any(), any()))
        .willReturn(mock(TelegramAction.class));
    Message message = new Message(4234, user, chat, "/cat", null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isValuePresent()).isTrue();
    HandlingResult handlingResult = result.getValue();
    assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.HANDLED);
    assertThat(handlingResult.getActions()).hasSize(1);
    verify(actionFactory).newSendMessageAction(chat, "url");
    verifyNoMoreInteractions(actionFactory);
    verify(catFinder).getCat();
    verifyNoMoreInteractions(catFinder);
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
    verifyNoMoreInteractions(actionFactory, catFinder);
  }

  @Test
  void handleCommandWithException() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    given(actionFactory.newSendMessageAction(any(), any())).willThrow(RuntimeException.class);
    given(catFinder.getCat()).willReturn(Exceptional.exceptional(new Cat("test")));
    Message message = new Message(4234, user, chat, "/cat", null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isException()).isTrue();
    result.ifException(e -> assertThat(e).isExactlyInstanceOf(RuntimeException.class));
    verify(actionFactory).newSendMessageAction(any(), any());
    verify(catFinder).getCat();
    verifyNoMoreInteractions(catFinder, actionFactory);
  }

  @Test
  void handleCommandWithGettingCatException() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    given(actionFactory.newSendMessageAction(any(), any()))
        .willReturn(mock(TelegramAction.class));
    given(catFinder.getCat()).willReturn(Exceptional.exceptional(new RuntimeException()));
    Message message = new Message(4234, user, chat, "/cat", null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isException()).isFalse();
    verify(actionFactory).newSendMessageAction(chat,
        "Sorry, your cat can't be delivered now, please try later");
    verify(catFinder).getCat();
    verifyNoMoreInteractions(catFinder, actionFactory);
  }
}
