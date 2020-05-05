package ru.romangr.catbot.statistic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StatisticServiceTest {

    @Test
    internal fun initialState() {
        val statisticService = StatisticService()

        val statistics = statisticService.commandStatistics()

        assertThat(statistics.sinceDate).isToday()
        assertThat(statistics.stats).isEmpty()
    }

    @Test
    internal fun registerActions() {
        val statisticService = StatisticService()

        statisticService.registerAction("action1")
        statisticService.registerAction("action2")
        statisticService.registerAction("action2")
        val (sinceDate, stats) = statisticService.commandStatistics()

        assertThat(sinceDate).isToday()
        assertThat(stats.keys).containsOnly("action1", "action2")
        assertThat(stats["action1"]).isEqualTo(1)
        assertThat(stats["action2"]).isEqualTo(2)
    }
}
