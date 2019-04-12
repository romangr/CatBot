package ru.romangr.lolbot.handler;

import org.junit.jupiter.api.Test;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.lolbot.telegram.model.Chat;
import ru.romangr.lolbot.telegram.model.Message;
import ru.romangr.lolbot.telegram.model.Update;
import ru.romangr.lolbot.telegram.model.User;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class UpdatesHandlerTest {

    private MessagePreprocessor messagePreprocessor = mock(MessagePreprocessor.class);
    private UnknownCommandHandler unknownCommandHandler = mock(UnknownCommandHandler.class);
    private CommandHandler commandHandler1 = mock(CommandHandler.class);
    private CommandHandler commandHandler2 = mock(CommandHandler.class);
    private final List<CommandHandler> commandHandlers = List.of(commandHandler1, commandHandler2);
    private UpdatesHandler updatesHandler
            = new UpdatesHandler(messagePreprocessor, commandHandlers, unknownCommandHandler);

    @Test
    void handleSuccessfully() {
        given(messagePreprocessor.process(anyString())).willReturn("/preprocessed");
        given(commandHandler1.handle(any(), any()))
                .willReturn(Exceptional.exceptional(HandlingResult.HANDLED));
        given(commandHandler2.handle(any(), any()))
                .willReturn(Exceptional.exceptional(HandlingResult.SKIPPED));

        Chat chat = Chat.builder()
                .firstName("first")
                .lastName("last")
                .title("title")
                .id(123123)
                .username("username")
                .build();

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
        verifyNoMoreInteractions(messagePreprocessor, commandHandler1, commandHandler2);
        verifyZeroInteractions(unknownCommandHandler);
    }

    @Test
    void handleWithException() {
        given(messagePreprocessor.process(anyString())).willReturn("/preprocessed");
        given(commandHandler1.handle(any(), any()))
                .willReturn(Exceptional.exceptional(HandlingResult.HANDLED));
        given(commandHandler2.handle(any(), any()))
                .willReturn(Exceptional.exceptional(new RuntimeException()));

        Chat chat = Chat.builder()
                .firstName("first")
                .lastName("last")
                .title("title")
                .id(123123)
                .username("username")
                .build();

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
        verifyNoMoreInteractions(messagePreprocessor, commandHandler1, commandHandler2);
        verifyZeroInteractions(unknownCommandHandler);
    }

    @Test
    void handleUnknownCommand() {
        given(messagePreprocessor.process(anyString())).willReturn("/preprocessed");
        given(commandHandler1.handle(any(), any()))
                .willReturn(Exceptional.exceptional(HandlingResult.SKIPPED));
        given(commandHandler2.handle(any(), any()))
                .willReturn(Exceptional.exceptional(HandlingResult.SKIPPED));
        given(unknownCommandHandler.handle(any()))
                .willReturn(Exceptional.exceptional(HandlingResult.HANDLED));

        Chat chat = Chat.builder()
                .firstName("first")
                .lastName("last")
                .title("title")
                .id(123123)
                .username("username")
                .build();

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
        verifyNoMoreInteractions(messagePreprocessor, commandHandler1, commandHandler2,
                unknownCommandHandler);
    }

}