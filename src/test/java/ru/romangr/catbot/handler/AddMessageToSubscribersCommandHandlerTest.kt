package ru.romangr.catbot.handler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.verifyZeroInteractions
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.test.utils.anyChat

internal class AddMessageToSubscribersCommandHandlerTest {

    private val subscribersService: SubscribersService = mock(SubscribersService::class.java)
    private val actionFactory: TelegramActionFactory = mock(TelegramActionFactory::class.java)
    private val handler = AddMessageToSubscribersCommandHandler(subscribersService, actionFactory, 1)

    @Test
    internal fun validArgument() {
        val chat = Chat(1)
        given(actionFactory.newSendMessageAction(anyChat(), anyString()))
                .willReturn(mock(TelegramAction::class.java))
        given(subscribersService.messageQueueLength).willReturn(1)

        val result = handler.handle(chat, "/amts test")

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        assertThat(result.value.actions).hasSize(1)
        verify(actionFactory).newSendMessageAction(chat, "Message added to the queue, there are 1 message(s)")
        verify(subscribersService).addMessageToSubscribers("test")
        verify(subscribersService).messageQueueLength
        verifyNoMoreInteractions(subscribersService, actionFactory)
    }

    @Test
    internal fun complexArgument() {
        val chat = Chat(1)
        given(actionFactory.newSendMessageAction(anyChat(), anyString()))
                .willReturn(mock(TelegramAction::class.java))
        given(subscribersService.messageQueueLength).willReturn(1)

        val result = handler.handle(chat, "/amts test\ntest test")

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        assertThat(result.value.actions).hasSize(1)
        verify(actionFactory).newSendMessageAction(chat, "Message added to the queue, there are 1 message(s)")
        verify(subscribersService).addMessageToSubscribers("test\ntest test")
        verify(subscribersService).messageQueueLength
        verifyNoMoreInteractions(subscribersService, actionFactory)
    }

    @Test
    internal fun emptyArgument() {
        val chat = Chat(1)
        given(actionFactory.newSendMessageAction(anyChat(), anyString()))
                .willReturn(mock(TelegramAction::class.java))

        val result = handler.handle(chat, "/amts    ")

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        assertThat(result.value.actions).hasSize(1)
        verify(actionFactory).newSendMessageAction(chat, "Argument is empty or blank")
        verifyZeroInteractions(subscribersService, actionFactory)
    }

    @Test
    internal fun notAdmin() {
        val chat = Chat(101)
        val result = handler.handle(chat, "/amts test")

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        assertThat(result.value.actions).hasSize(0)
        verifyZeroInteractions(subscribersService, actionFactory)
    }

    @Test
    internal fun notApplicable() {
        val chat = Chat(101)
        val result = handler.handle(chat, "/some-command test")

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.SKIPPED)
        assertThat(result.value.actions).hasSize(0)
        verifyZeroInteractions(subscribersService, actionFactory)
    }
}
