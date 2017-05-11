package Utils;

import org.junit.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Roman 12.05.2017.
 */
public class DelayCalculatorTest {
    @Test
    public void calculateDelayToRunAtParticularTime() throws Exception {
        ZonedDateTime zonedNow = ZonedDateTime.now();
        ZonedDateTime nextIntegerTime = zonedNow.plusHours(1).withMinute(0).withSecond(0);
        Duration expectedDelay = Duration.between(zonedNow, nextIntegerTime);
        Duration delay = DelayCalculator.calculateDelayToRunAtParticularTime(zonedNow.plusHours(1).getHour());
        assertThat(delay, is(expectedDelay));
    }

    @Test
    public void getSecondsFromHours() throws Exception {
        Random random = new Random(new Date().getTime());
        int randomInt = random.nextInt(10);
        assertThat(DelayCalculator.getSecondsFromHours(randomInt), is(randomInt * 60 * 60));
    }

}