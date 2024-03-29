package ru.romangr.catbot.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.util.CollectionUtils;
import ru.romangr.catbot.executor.TelegramActionExecutor;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.Message;
import ru.romangr.catbot.telegram.model.Update;
import ru.romangr.catbot.telegram.model.User;
import ru.romangr.exceptional.Exceptional;

class UpdatesHandlerTest {

  private MessagePreprocessor messagePreprocessor = mock(MessagePreprocessor.class);
  private TelegramActionExecutor actionExecutor = mock(TelegramActionExecutor.class);
  private UnknownCommandHandler unknownCommandHandler = mock(UnknownCommandHandler.class);
  private CommandHandler commandHandler1 = mock(CommandHandler.class);
  private CommandHandler commandHandler2 = mock(CommandHandler.class);
  private final List<CommandHandler> commandHandlers = List.of(commandHandler1, commandHandler2);
  private UpdatesHandler updatesHandler
      = new UpdatesHandler(messagePreprocessor, commandHandlers, unknownCommandHandler,
      actionExecutor);
  private ArgumentCaptor<List<TelegramAction>> actionsCaptor = ArgumentCaptor.forClass(List.class);

  @Test
  void handleSuccessfully() {
    Chat chat = new Chat(123123, "title", "first", "last", "username");
    User user = User.builder()
        .firstName("first")
        .lastName("last")
        .id(123123)
        .username("username")
        .build();
    Message preprocessedMessage = new Message(213, user, chat, "/preprocessed", null, null, null);
    given(messagePreprocessor.process(any(Message.class))).willReturn(preprocessedMessage);
    given(commandHandler1.handle(any(), any())).willReturn(
        Exceptional.exceptional(
            new HandlingResult(
                List.of(mock(TelegramAction.class)),
                HandlingStatus.HANDLED
            )
        )
    );
    given(commandHandler2.handle(any(), any()))
        .willReturn(Exceptional.exceptional(new HandlingResult(
            List.of(),
            HandlingStatus.SKIPPED
        )));

    Message message = new Message(345, user, chat, "/test", null, null, null);
    Update update = Update.builder()
        .id(0)
        .message(message)
        .build();

    updatesHandler.handleUpdate(update);

    verify(messagePreprocessor).process(message);
    verify(commandHandler1).handle(chat, preprocessedMessage);
    verify(commandHandler2).handle(chat, preprocessedMessage);
    verify(actionExecutor).execute(actionsCaptor.capture());
    List<TelegramAction> actions = actionsCaptor.getValue();
    assertThat(actions).hasSize(1);
    verifyNoMoreInteractions(messagePreprocessor, commandHandler1, commandHandler2, actionExecutor);
    verifyNoMoreInteractions(unknownCommandHandler);
  }

  @Test
  void handleWithException() {
    Chat chat = new Chat(123123, "title", "first", "last", "username");
    User user = User.builder()
        .firstName("first")
        .lastName("last")
        .id(123123)
        .username("username")
        .build();
    Message preprocessedMessage = new Message(213, user, chat, "/preprocessed", null, null, null);
    given(messagePreprocessor.process(any(Message.class))).willReturn(preprocessedMessage);
    given(commandHandler1.handle(any(), any()))
        .willReturn(Exceptional.exceptional(
            new HandlingResult(
                List.of(mock(TelegramAction.class)),
                HandlingStatus.HANDLED
            )
        ));
    given(commandHandler2.handle(any(), any()))
        .willReturn(Exceptional.exceptional(new RuntimeException()));

    Message message = new Message(345, user, chat, "/test", null, null, null);
    Update update = Update.builder()
        .id(0)
        .message(message)
        .build();

    updatesHandler.handleUpdate(update);

    verify(messagePreprocessor).process(message);
    verify(commandHandler1).handle(chat, preprocessedMessage);
    verify(commandHandler2).handle(chat, preprocessedMessage);
    verify(actionExecutor).execute(actionsCaptor.capture());
    List<TelegramAction> actions = actionsCaptor.getValue();
    assertThat(actions).hasSize(1);
    verifyNoMoreInteractions(messagePreprocessor, commandHandler1, commandHandler2, actionExecutor);
    verifyNoMoreInteractions(unknownCommandHandler);
  }

  @Test
  void handleUnknownCommand() {
    Chat chat = new Chat(123123, "title", "first", "last", "username");
    User user = User.builder()
        .firstName("first")
        .lastName("last")
        .id(123123)
        .username("username")
        .build();
    Message preprocessedMessage = new Message(213, user, chat, "/preprocessed", null, null, null);
    given(messagePreprocessor.process(any(Message.class))).willReturn(preprocessedMessage);
    given(commandHandler1.handle(any(), any()))
        .willReturn(Exceptional.exceptional(new HandlingResult(
            List.of(),
            HandlingStatus.SKIPPED
        )));
    given(commandHandler2.handle(any(), any()))
        .willReturn(Exceptional.exceptional(new HandlingResult(
            List.of(),
            HandlingStatus.SKIPPED
        )));
    TelegramAction unknownHandlerAction = mock(TelegramAction.class);
    given(unknownCommandHandler.handle(any()))
        .willReturn(Exceptional.exceptional(
            new HandlingResult(
                List.of(unknownHandlerAction),
                HandlingStatus.HANDLED
            )
        ));

    Message message = new Message(345, user, chat, "/test", null, null, null);
    Update update = Update.builder()
        .id(0)
        .message(message)
        .build();

    updatesHandler.handleUpdate(update);

    verify(messagePreprocessor).process(message);
    verify(commandHandler1).handle(chat, preprocessedMessage);
    verify(commandHandler2).handle(chat, preprocessedMessage);
    verify(unknownCommandHandler).handle(chat);

    verify(actionExecutor, times(2)).execute(actionsCaptor.capture());
    List<List<TelegramAction>> actions = actionsCaptor.getAllValues();
    long emptyActionLists = actions.stream()
        .filter(CollectionUtils::isEmpty)
        .count();
    assertThat(emptyActionLists).isEqualTo(1);
    long notEmptyActionLists = actions.stream()
        .filter(list -> !CollectionUtils.isEmpty(list))
        .filter(list -> list.contains(unknownHandlerAction))
        .count();
    assertThat(notEmptyActionLists).isEqualTo(1);

    verifyNoMoreInteractions(messagePreprocessor, commandHandler1, commandHandler2,
        unknownCommandHandler, actionExecutor);
  }

}
