package ru.romangr.catbot.statistic

import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BiFunction

class StatisticService {

    private val statsPerWeek: ConcurrentHashMap<String, AtomicInteger> = ConcurrentHashMap()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    @Volatile private var date: LocalDate = LocalDate.now(UTC)

    init {
        scheduler.scheduleAtFixedRate({
            statsPerWeek.clear()
            date = LocalDate.now(UTC)
        }, 7, 7, TimeUnit.DAYS)
    }

    fun registerAction(action: String) {
        statsPerWeek.compute(action, BiFunction { _, value ->
            if (value == null) {
                return@BiFunction AtomicInteger(1)
            }
            value.incrementAndGet()
            return@BiFunction value
        })
    }

    fun commandStatistics(): ActionStatistics {
        val mapWithInt = statsPerWeek.mapValues { it.value.toInt() }
        return ActionStatistics(date, mapWithInt)
    }

}

data class ActionStatistics(val sinceDate: LocalDate, val stats: Map<String, Int>)
