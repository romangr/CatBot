package ru.romangr.catbot.handler;

import org.junit.jupiter.api.Test;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.catbot.handler.action.TelegramAction;
import ru.romangr.catbot.handler.action.TelegramActionFactory;
import ru.romangr.catbot.telegram.model.Chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class HelpCommandHandlerTest {

    private TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
    private CommandHandler handler = new HelpCommandHandler(actionFactory);

    @Test
    void handleCommandSuccessfully() {
        Chat chat = Chat.builder()
                .id(1)
                .build();
        given(actionFactory.newSendMessageAction(any(), any()))
                .willReturn(mock(TelegramAction.class));

        Exceptional<HandlingResult> result = handler.handle(chat, "/help");

        assertThat(result.isValuePresent()).isTrue();
        HandlingResult handlingResult = result.getValue();
        assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.HANDLED);
        assertThat(handlingResult.getActions()).hasSize(1);
        verify(actionFactory)
                .newSendMessageAction(chat, "Type /cat to get a random cat :3");
        verifyNoMoreInteractions(actionFactory);
    }

    @Test
    void skipUnknownCommand() {
        Chat chat = Chat.builder()
                .id(1)
                .build();

        Exceptional<HandlingResult> result = handler.handle(chat, "unknown");

        assertThat(result.isValuePresent()).isTrue();
        HandlingResult handlingResult = result.getValue();
        assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.SKIPPED);
        assertThat(handlingResult.getActions()).isEmpty();
        verifyZeroInteractions(actionFactory);
    }

    @Test
    void handleCommandWithException() {
        Chat chat = Chat.builder()
                .id(1)
                .build();
        given(actionFactory.newSendMessageAction(any(), any()))
                .willThrow(RuntimeException.class);

        Exceptional<HandlingResult> result = handler.handle(chat, "/help");

        assertThat(result.isException()).isTrue();
        result.ifException(e -> assertThat(e).isExactlyInstanceOf(RuntimeException.class));
        verify(actionFactory).newSendMessageAction(eq(chat), anyString());
    }

}
