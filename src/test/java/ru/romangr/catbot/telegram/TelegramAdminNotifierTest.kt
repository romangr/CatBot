package ru.romangr.catbot.telegram

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import ru.romangr.catbot.executor.TelegramActionExecutor
import ru.romangr.catbot.executor.action.TelegramAction
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.test.utils.anyChat
import ru.romangr.catbot.utils.PropertiesResolver

internal class TelegramAdminNotifierTest {

    private val actionFactory = mock(TelegramActionFactory::class.java)
    private val actionExecutor = mock(TelegramActionExecutor::class.java)
    private val propertiesResolver = mock(PropertiesResolver::class.java)
    @SuppressWarnings("unchecked")
    private val actionCaptor = ArgumentCaptor.forClass<List<TelegramAction>, List<TelegramAction>>(List::class.java as Class<List<TelegramAction>>)

    @Test
    internal fun buildInfoIsSentOnStartWhenAdminChatIdIsPresent() {
        val action = mock(TelegramAction::class.java)
        given(actionFactory.newSendMessageAction(anyChat(), anyString()))
                .willReturn(action)
        given(propertiesResolver.buildInfo).willReturn("info")
        given(propertiesResolver.updatesCheckPeriod).willReturn(30)
        given(propertiesResolver.timeToSendMessageToSubscribers).willReturn(17)
        val notifier = TelegramAdminNotifier(actionFactory, actionExecutor, propertiesResolver, 1L)

        notifier.botStarted(15)

        verify(actionFactory).newSendMessageAction(Chat(1),
                """Bot started! info
                |Updates check period: 30
                |Time to send a message to subscribers: 17
                |Number of subscribers: 15
            """.trimMargin())
        verify(propertiesResolver).buildInfo
        verify(propertiesResolver).updatesCheckPeriod
        verify(propertiesResolver).timeToSendMessageToSubscribers
        verify(actionExecutor).execute(actionCaptor.capture())
        assertThat(actionCaptor.value).hasSize(1).first().isSameAs(action)
        verifyNoMoreInteractions(actionFactory, actionExecutor, propertiesResolver)
    }

    @Test
    internal fun buildInfoIsNotSentOnStartWhenAdminChatIdIsNotPresent() {
        given(propertiesResolver.buildInfo).willReturn("info")
        given(propertiesResolver.updatesCheckPeriod).willReturn(30)
        given(propertiesResolver.timeToSendMessageToSubscribers).willReturn(17)
        val notifier = TelegramAdminNotifier(actionFactory, actionExecutor, propertiesResolver, null)

        notifier.botStarted(15)

        verify(propertiesResolver).buildInfo
        verify(propertiesResolver).updatesCheckPeriod
        verify(propertiesResolver).timeToSendMessageToSubscribers
        verifyNoMoreInteractions(propertiesResolver)
        verifyNoMoreInteractions(actionFactory, actionExecutor)
    }
}
