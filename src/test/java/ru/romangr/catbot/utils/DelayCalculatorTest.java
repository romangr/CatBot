package ru.romangr.catbot.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Roman 12.05.2017.
 */
class DelayCalculatorTest {

    @Test
    void calculateDelayToRunAtParticularTime() {
        ZonedDateTime zonedNow = ZonedDateTime.now(Clock.systemUTC());
        ZonedDateTime nextIntegerTime = zonedNow.plusHours(1).withMinute(0).withSecond(0);
        Duration expectedDelay = Duration.between(zonedNow, nextIntegerTime);
        Duration delay = DelayCalculator.calculateDelayToRunAtParticularTime(zonedNow.plusHours(1).getHour());
        assertThat(delay).isEqualTo(expectedDelay);
    }

    @Test
    void getSecondsFromHours() {
        Random random = new Random(new Date().getTime());
        int randomInt = random.nextInt(10);
        assertThat(DelayCalculator.getSecondsFromHours(randomInt)).isEqualTo(randomInt * 60 * 60);
    }
}
