package ru.romangr.catbot.handler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.catbot.telegram.model.User
import ru.romangr.catbot.test.utils.anyChat

internal class AddMessageToSubscribersCommandHandlerTest {

    private val subscribersService: SubscribersService = mock(SubscribersService::class.java)
    private val actionFactory: TelegramActionFactory = mock(TelegramActionFactory::class.java)
    private val handler = AddMessageToSubscribersCommandHandler(subscribersService, actionFactory, 1)

    @Test
    internal fun validArgument() {
        val chat = Chat(1)
        val user = User.builder().id(1).build()
        given(actionFactory.newSendMessageAction(anyChat(), anyString()))
                .willReturn(mock(TelegramAction::class.java))
        given(subscribersService.messageQueueLength).willReturn(1)
        val message = Message(text = "/amts test", chat = chat, from = user, id = 213)

        val result = handler.handle(chat, message)

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        assertThat(result.value.actions).hasSize(1)
        verify(actionFactory).newSendMessageAction(chat, "Message is added to the queue, there are 1 message(s)")
        verify(subscribersService).addMessageToSubscribers(MessageToSubscribers.textMessage("test"))
        verify(subscribersService).messageQueueLength
        verifyNoMoreInteractions(subscribersService, actionFactory)
    }

    @Test
    internal fun complexArgument() {
        val chat = Chat(1)
        val user = User.builder().id(1).build()
        given(actionFactory.newSendMessageAction(anyChat(), anyString()))
                .willReturn(mock(TelegramAction::class.java))
        given(subscribersService.messageQueueLength).willReturn(1)
        val message = Message(text = "/amts test\ntest test", chat = chat, from = user, id = 213)

        val result = handler.handle(chat, message)

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        assertThat(result.value.actions).hasSize(1)
        verify(actionFactory).newSendMessageAction(chat, "Message is added to the queue, there are 1 message(s)")
        verify(subscribersService).addMessageToSubscribers(MessageToSubscribers.textMessage("test\ntest test"))
        verify(subscribersService).messageQueueLength
        verifyNoMoreInteractions(subscribersService, actionFactory)
    }

    @Test
    internal fun emptyArgument() {
        val chat = Chat(1)
        val user = User.builder().id(1).build()
        given(actionFactory.newSendMessageAction(anyChat(), anyString()))
                .willReturn(mock(TelegramAction::class.java))
        val message = Message(text = "/amts    ", chat = chat, from = user, id = 213)

        val result = handler.handle(chat, message)

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        assertThat(result.value.actions).hasSize(1)
        verify(actionFactory).newSendMessageAction(chat, "Argument is empty or blank")
        verifyNoMoreInteractions(subscribersService, actionFactory)
    }

    @Test
    internal fun notAdmin() {
        val chat = Chat(101)
        val user = User.builder().id(101).build()
        val message = Message(text = "/amts test", chat = chat, from = user, id = 213)

        val result = handler.handle(chat, message)

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        assertThat(result.value.actions).hasSize(0)
        verifyNoMoreInteractions(subscribersService, actionFactory)
    }

    @Test
    internal fun notApplicable() {
        val chat = Chat(101)
        val user = User.builder().id(101).build()
        val message = Message(text = "/some-command test", chat = chat, from = user, id = 213)

        val result = handler.handle(chat, message)

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.SKIPPED)
        assertThat(result.value.actions).hasSize(0)
        verifyNoMoreInteractions(subscribersService, actionFactory)
    }
}
