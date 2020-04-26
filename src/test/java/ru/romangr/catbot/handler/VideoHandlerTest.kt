package ru.romangr.catbot.handler

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.mockito.Mockito
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.dto.Video
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.catbot.telegram.model.User
import ru.romangr.catbot.test.utils.anyChat

internal class VideoHandlerTest {

    private val subscribersService: SubscribersService = Mockito.mock(SubscribersService::class.java)
    private val actionFactory: TelegramActionFactory = Mockito.mock(TelegramActionFactory::class.java)
    private val handler = VideoHandler(actionFactory, subscribersService, 1)

    @Test
    internal fun validArgument() {
        val chat = Chat(1)
        val user = User.builder().id(1).build()
        BDDMockito.given(actionFactory.newSendMessageAction(anyChat(), ArgumentMatchers.anyString()))
                .willReturn(Mockito.mock(TelegramAction::class.java))
        BDDMockito.given(subscribersService.messageQueueLength).willReturn(1)
        val message = Message(video = Video("id"), chat = chat, from = user, id = 213)

        val result = handler.handle(chat, message)

        Assertions.assertThat(result.isValuePresent).isTrue()
        Assertions.assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        Assertions.assertThat(result.value.actions).hasSize(1)
        Mockito.verify(actionFactory).newSendMessageAction(chat, "Video is added to the queue, there are 1 message(s)")
        Mockito.verify(subscribersService).addMessageToSubscribers(MessageToSubscribers.videoMessage("id"))
        Mockito.verify(subscribersService).messageQueueLength
        Mockito.verifyNoMoreInteractions(subscribersService, actionFactory)
    }

    @Test
    internal fun notAdmin() {
        val chat = Chat(101)
        val user = User.builder().id(101).build()
        val message = Message(video = Video("id"), chat = chat, from = user, id = 213)

        val result = handler.handle(chat, message)

        Assertions.assertThat(result.isValuePresent).isTrue()
        Assertions.assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        Assertions.assertThat(result.value.actions).hasSize(0)
        Mockito.verifyNoMoreInteractions(subscribersService, actionFactory)
    }

    @Test
    internal fun notApplicable() {
        val chat = Chat(101)
        val user = User.builder().id(101).build()
        val message = Message(text = "/some-command test", chat = chat, from = user, id = 213)

        val result = handler.handle(chat, message)

        Assertions.assertThat(result.isValuePresent).isTrue()
        Assertions.assertThat(result.value.status).isEqualTo(HandlingStatus.SKIPPED)
        Assertions.assertThat(result.value.actions).hasSize(0)
        Mockito.verifyNoMoreInteractions(subscribersService, actionFactory)
    }
}
