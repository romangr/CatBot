package ru.romangr.catbot.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
    given(messagePreprocessor.process(anyString())).willReturn("/preprocessed");
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

    Chat chat = new Chat(123123);
    chat.setFirstName("first");
    chat.setLastName("last");
    chat.setTitle("title");
    chat.setUsername("username");

    User user = User.builder()
        .firstName("first")
        .lastName("last")
        .id(123123)
        .username("username")
        .build();

    Update update = Update.builder()
        .id(0)
        .message(Message.builder()
            .chat(chat)
            .from(user)
            .text("/test")
            .build())
        .build();

    updatesHandler.handleUpdate(update);

    verify(messagePreprocessor).process("/test");
    verify(commandHandler1).handle(chat, "/preprocessed");
    verify(commandHandler2).handle(chat, "/preprocessed");
    verify(actionExecutor).execute(actionsCaptor.capture());
    List<TelegramAction> actions = actionsCaptor.getValue();
    assertThat(actions).hasSize(1);
    verifyNoMoreInteractions(messagePreprocessor, commandHandler1, commandHandler2, actionExecutor);
    verifyZeroInteractions(unknownCommandHandler);
  }

  @Test
  void handleWithException() {
    given(messagePreprocessor.process(anyString())).willReturn("/preprocessed");
    given(commandHandler1.handle(any(), any()))
        .willReturn(Exceptional.exceptional(
            new HandlingResult(
                List.of(mock(TelegramAction.class)),
                HandlingStatus.HANDLED
            )
        ));
    given(commandHandler2.handle(any(), any()))
        .willReturn(Exceptional.exceptional(new RuntimeException()));

    Chat chat = new Chat(123123);
    chat.setFirstName("first");
    chat.setLastName("last");
    chat.setTitle("title");
    chat.setUsername("username");

    User user = User.builder()
        .firstName("first")
        .lastName("last")
        .id(123123)
        .username("username")
        .build();

    Update update = Update.builder()
        .id(0)
        .message(Message.builder()
            .chat(chat)
            .from(user)
            .text("/test")
            .build())
        .build();

    updatesHandler.handleUpdate(update);

    verify(messagePreprocessor).process("/test");
    verify(commandHandler1).handle(chat, "/preprocessed");
    verify(commandHandler2).handle(chat, "/preprocessed");
    verify(actionExecutor).execute(actionsCaptor.capture());
    List<TelegramAction> actions = actionsCaptor.getValue();
    assertThat(actions).hasSize(1);
    verifyNoMoreInteractions(messagePreprocessor, commandHandler1, commandHandler2, actionExecutor);
    verifyZeroInteractions(unknownCommandHandler);
  }

  @Test
  void handleUnknownCommand() {
    given(messagePreprocessor.process(anyString())).willReturn("/preprocessed");
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
    given(unknownCommandHandler.handle(any()))
        .willReturn(Exceptional.exceptional(
            new HandlingResult(
                List.of(mock(TelegramAction.class)),
                HandlingStatus.HANDLED
            )
        ));

    Chat chat = new Chat(123123);
    chat.setFirstName("first");
    chat.setLastName("last");
    chat.setTitle("title");
    chat.setUsername("username");

    User user = User.builder()
        .firstName("first")
        .lastName("last")
        .id(123123)
        .username("username")
        .build();

    Update update = Update.builder()
        .id(0)
        .message(Message.builder()
            .chat(chat)
            .from(user)
            .text("/test")
            .build())
        .build();

    updatesHandler.handleUpdate(update);

    verify(messagePreprocessor).process("/test");
    verify(commandHandler1).handle(chat, "/preprocessed");
    verify(commandHandler2).handle(chat, "/preprocessed");
    verify(unknownCommandHandler).handle(chat);

    verify(actionExecutor, times(2)).execute(actionsCaptor.capture());
    List<List<TelegramAction>> actions = actionsCaptor.getAllValues();
    assertThat(actions)
        .allMatch(List::isEmpty);

    verifyNoMoreInteractions(messagePreprocessor, commandHandler1, commandHandler2,
        unknownCommandHandler, actionExecutor);
  }

}
