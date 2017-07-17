package Utils;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Roman 11.05.2017.
 */
public class DelayCalculator {

    public static Duration calculateDelayToRunAtParticularTime(int oClock) {
        ZonedDateTime zonedNow = ZonedDateTime.now();
        ZonedDateTime zonedNext18 = zonedNow.withHour(oClock).withMinute(0).withSecond(0);
        if (zonedNow.compareTo(zonedNext18) > 0) {
            zonedNext18 = zonedNext18.plusDays(1);
        }
        return Duration.between(zonedNow, zonedNext18);
    }

    public static int getSecondsFromHours(int hours) {
        return hours * 60 * 60;
    }

    private DelayCalculator() {
    }
}
