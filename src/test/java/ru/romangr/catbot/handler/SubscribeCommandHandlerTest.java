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

class SubscribeCommandHandlerTest {

  private TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
  private SubscribersService subscribersService = mock(SubscribersService.class);
  private StatisticService statisticService = mock(StatisticService.class);
  private CommandHandler handler = new SubscribeCommandHandler(actionFactory, subscribersService, statisticService);

  @Test
  void handleCommandSuccessfullyForNewSubscriber() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    given(subscribersService.addSubscriber(any())).willReturn(Exceptional.exceptional(true));
    given(actionFactory.newSendMessageAction(any(), any()))
        .willReturn(mock(TelegramAction.class));
    given(subscribersService.getSubscribersCount()).willReturn(10);
    Message message = new Message(4234, user, chat, "/subscribe", null, null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isValuePresent()).isTrue();
    HandlingResult handlingResult = result.getValue();
    assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.HANDLED);
    assertThat(handlingResult.getActions()).hasSize(1);
    verify(actionFactory).newSendMessageAction(chat, "Thank you for subscription!");
    verifyNoMoreInteractions(actionFactory);
    verify(subscribersService).addSubscriber(chat);
    verify(subscribersService).getSubscribersCount();
    verifyNoMoreInteractions(subscribersService);
  }

  @Test
  void handleCommandSuccessfullyForOneWhoIsAlreadyASubscriber() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    given(subscribersService.addSubscriber(any())).willReturn(Exceptional.exceptional(false));
    given(actionFactory.newSendMessageAction(any(), any()))
        .willReturn(mock(TelegramAction.class));
    Message message = new Message(4234, user, chat, "/subscribe", null, null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isValuePresent()).isTrue();
    HandlingResult handlingResult = result.getValue();
    assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.HANDLED);
    assertThat(handlingResult.getActions()).hasSize(1);
    verify(actionFactory).newSendMessageAction(chat, "You have already subscribed!");
    verifyNoMoreInteractions(actionFactory);
    verify(subscribersService).addSubscriber(chat);
    verifyNoMoreInteractions(subscribersService);
  }

  @Test
  void skipUnknownCommand() {
    Chat chat = new Chat(1, null, null, null, null);
    User user = User.builder().id(1).build();
    Message message = new Message(4234, user, chat, "unknown", null, null, null);

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
    given(subscribersService.addSubscriber(any()))
        .willReturn(Exceptional.exceptional(new RuntimeException()));
    Message message = new Message(4234, user, chat, "/subscribe", null, null, null);

    Exceptional<HandlingResult> result = handler.handle(chat, message);

    assertThat(result.isValuePresent()).isTrue();
    HandlingResult handlingResult = result.getValue();
    assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.HANDLED);
    assertThat(handlingResult.getActions()).hasSize(1);
    verify(actionFactory).newSendMessageAction(chat,
        "Sorry, some error occurred when we tried to subscribe you, please try later");
    verifyNoMoreInteractions(actionFactory);
    verify(subscribersService).addSubscriber(chat);
    verifyNoMoreInteractions(subscribersService);
  }

}
