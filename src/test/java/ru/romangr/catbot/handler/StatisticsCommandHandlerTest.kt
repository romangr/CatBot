package ru.romangr.catbot.handler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import ru.romangr.catbot.executor.action.TelegramActionFactory
import ru.romangr.catbot.statistic.ActionStatistics
import ru.romangr.catbot.statistic.StatisticService
import ru.romangr.catbot.subscription.SubscribersService
import ru.romangr.catbot.telegram.model.Chat
import ru.romangr.catbot.telegram.model.Message
import ru.romangr.catbot.telegram.model.User
import java.time.LocalDate

internal class StatisticsCommandHandlerTest {

    private val actionFactory: TelegramActionFactory = mock(TelegramActionFactory::class.java)
    private val statisticService: StatisticService = mock(StatisticService::class.java)
    private val subscribersService: SubscribersService = mock(SubscribersService::class.java)
    private val handler = StatisticsCommandHandler(
            actionFactory,
            1,
            statisticService,
            subscribersService
    )

    @Test
    internal fun handleCommandSuccessfully() {
        val chat = Chat(1)
        val user = User.builder().id(1).build()
        val date = LocalDate.of(2020, 5, 4)
        val statistics = ActionStatistics(date, mapOf(Pair("action", 1)))
        given(statisticService.commandStatistics()).willReturn(statistics)
        given(subscribersService.subscribersCount).willReturn(10)
        val message = Message(text = "/stats", chat = chat, from = user, id = 213)

        val result = handler.handle(chat, message)

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        assertThat(result.value.actions).hasSize(1)
        verify(actionFactory).newSendMessageAction(chat, "Action statistics since 2020-05-04:\n  action: 1\n\nSubscribers: 10")
    }

    @Test
    internal fun notAdmin() {
        val chat = Chat(101)
        val user = User.builder().id(101).build()
        val message = Message(text = "/stats", chat = chat, from = user, id = 213)

        val result = handler.handle(chat, message)

        assertThat(result.isValuePresent).isTrue()
        assertThat(result.value.status).isEqualTo(HandlingStatus.HANDLED)
        assertThat(result.value.actions).hasSize(0)
        verify(statisticService).registerAction("StatisticsCommandHandler")
        verifyNoMoreInteractions(statisticService, actionFactory)
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
        verifyNoMoreInteractions(statisticService, actionFactory)
    }
}
