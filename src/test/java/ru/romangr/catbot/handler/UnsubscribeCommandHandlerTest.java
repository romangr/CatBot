package ru.romangr.catbot.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.statistic.StatisticService;
import ru.romangr.catbot.subscription.SubscribersService;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.Message;
import ru.romangr.catbot.telegram.model.User;
import ru.romangr.exceptional.Exceptional;

class UnsubscribeCommandHandlerTest {

  private TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
  private SubscribersService subscribersService = mock(SubscribersService.class);
  private StatisticService statisticService = mock(StatisticService.class);
  private CommandHandler handler = new UnsubscribeCommandHandler(actionFactory, subscribersService, statisticService);

  @Test
  void handleCommandSuccessfullyForASubscriber() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    given(subscribersService.deleteSubscriber(any())).willReturn(true);
    given(actionFactory.newSendMessageAction(any(), any()))
        .willReturn(mock(TelegramAction.class));
    given(subscribersService.getSubscribersCount()).willReturn(10);
    Message message = new Message(4234, user, chat, "/unsubscribe", null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isValuePresent()).isTrue();
    HandlingResult handlingResult = result.getValue();
    assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.HANDLED);
    assertThat(handlingResult.getActions()).hasSize(1);
    verify(actionFactory).newSendMessageAction(chat, "You have unsubscribed!");
    verifyNoMoreInteractions(actionFactory);
    verify(subscribersService).deleteSubscriber(chat);
    verify(subscribersService).getSubscribersCount();
    verifyNoMoreInteractions(subscribersService);
  }

  @Test
  void handleCommandSuccessfullyForOneWhoIsNotYetASubscriber() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    given(subscribersService.deleteSubscriber(any())).willReturn(false);
    given(actionFactory.newSendMessageAction(any(), any()))
        .willReturn(mock(TelegramAction.class));
    Message message = new Message(4234, user, chat, "/unsubscribe", null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isValuePresent()).isTrue();
    HandlingResult handlingResult = result.getValue();
    assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.HANDLED);
    assertThat(handlingResult.getActions()).hasSize(1);
    verify(actionFactory).newSendMessageAction(chat, "You are not a subscriber yet!");
    verifyNoMoreInteractions(actionFactory);
    verify(subscribersService).deleteSubscriber(chat);
    verifyNoMoreInteractions(subscribersService);
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
    verifyNoMoreInteractions(actionFactory, subscribersService);
  }

  @Test
  void handleCommandWithException() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    given(subscribersService.deleteSubscriber(any())).willThrow(RuntimeException.class);
    Message message = new Message(4234, user, chat, "/unsubscribe", null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isException()).isTrue();
    result.ifException(e -> assertThat(e).isExactlyInstanceOf(RuntimeException.class));
    verifyNoMoreInteractions(actionFactory);
    verify(subscribersService).deleteSubscriber(chat);
    verifyNoMoreInteractions(subscribersService);
  }

}
