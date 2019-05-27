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

class HelpCommandHandlerTest {

    private TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
    private CommandHandler handler = new HelpCommandHandler(actionFactory);

    @Test
    void handleCommandSuccessfully() {
        Chat chat = new Chat(1);
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
        Chat chat = new Chat(1);

        Exceptional<HandlingResult> result = handler.handle(chat, "unknown");

        assertThat(result.isValuePresent()).isTrue();
        HandlingResult handlingResult = result.getValue();
        assertThat(handlingResult.getStatus()).isEqualTo(HandlingStatus.SKIPPED);
        assertThat(handlingResult.getActions()).isEmpty();
        verifyZeroInteractions(actionFactory);
    }

    @Test
    void handleCommandWithException() {
        Chat chat = new Chat(1);
        given(actionFactory.newSendMessageAction(any(), any()))
                .willThrow(RuntimeException.class);

        Exceptional<HandlingResult> result = handler.handle(chat, "/help");

        assertThat(result.isException()).isTrue();
        result.ifException(e -> assertThat(e).isExactlyInstanceOf(RuntimeException.class));
        verify(actionFactory).newSendMessageAction(eq(chat), anyString());
    }

}
