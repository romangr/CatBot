package ru.romangr.catbot.executor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import ru.romangr.catbot.telegram.model.Chat;

class RateLimiterTest {

  private static final Duration limitPeriod = Duration.ofSeconds(5);
  private static final Duration banTimeout = Duration.ofSeconds(5);
  private RateLimiter rateLimiter = new RateLimiter(limitPeriod, banTimeout);

  @Test
  void simpleExecutionWithoutBans() {
    Chat chat = getChat(getRandomInt());
    List<RateLimitResult> checkResults =  IntStream.range(0, 20)
        .mapToObj(i -> rateLimiter.check(chat))
        .collect(Collectors.toList());

    assertThat(checkResults).allMatch(Predicate.isEqual(RateLimitResult.POSITIVE));
  }

  @Test
  void executionAfterLimitPeriod() throws InterruptedException {
    Chat chat = getChat(getRandomInt());
    IntStream.range(0, 25)
        .forEach(i -> rateLimiter.check(chat));

    Thread.sleep(durationToSleep(limitPeriod));

    List<RateLimitResult> checkResults =  IntStream.range(0, 20)
        .mapToObj(i -> rateLimiter.check(chat))
        .collect(Collectors.toList());

    assertThat(checkResults).allMatch(Predicate.isEqual(RateLimitResult.POSITIVE));
  }

  @Test
  void spreadExecutions() throws InterruptedException {
    Chat chat = getChat(getRandomInt());
    for (int i = 0; i < 30; i++) {
      assertThat(rateLimiter.check(chat))
          .withFailMessage(String.format("Failed when i = %s", i))
          .isEqualTo(RateLimitResult.POSITIVE);
      // two periods with evenly spread actions, less than limit value in the result
      Thread.sleep(limitPeriod.multipliedBy(2).dividedBy(30).toMillis());
    }
  }

  @Test
  void simpleExecutionWithBanResult() {
    Chat chat = getChat(getRandomInt());
    List<RateLimitResult> checkResults =  IntStream.range(0, 22)
        .mapToObj(i -> rateLimiter.check(chat))
        .collect(Collectors.toList());

    List<RateLimitResult> firstTwentyResults = checkResults.subList(0, checkResults.size() - 2);
    assertThat(firstTwentyResults).allMatch(Predicate.isEqual(RateLimitResult.POSITIVE));
    assertThat(checkResults.get(checkResults.size() - 2)).isEqualTo(RateLimitResult.MADE_BANNED);
    assertThat(checkResults.get(checkResults.size() - 1)).isEqualTo(RateLimitResult.BANNED);
  }

  @Test
  void unbanAfterTimeout() throws InterruptedException {
    Chat chat = getChat(getRandomInt());
    IntStream.range(0, 25)
        .forEach(i -> rateLimiter.check(chat));

    Thread.sleep(durationToSleep(banTimeout));

    assertThat(rateLimiter.check(chat)).isEqualTo(RateLimitResult.POSITIVE);
  }

  @Test
  void bannedAfterTimeoutIfThereWereSomeActions() throws InterruptedException {
    Chat chat = getChat(getRandomInt());
    IntStream.range(0, 25)
        .forEach(i -> rateLimiter.check(chat));

    Thread.sleep(banTimeout.dividedBy(2).toMillis());
    IntStream.range(0, 25)
        .forEach(i -> rateLimiter.check(chat));
    Thread.sleep(durationToSleep(banTimeout.dividedBy(2)));

    assertThat(rateLimiter.check(chat)).isEqualTo(RateLimitResult.BANNED);
  }

  private long durationToSleep(Duration duration) {
    return duration.toMillis() + 100;
  }

  private static Chat getChat(int id) {
    Chat chat = new Chat(id);
    chat.setFirstName("test");
    return chat;
  }

  private static int getRandomInt() {
    return ThreadLocalRandom.current().nextInt(100000);
  }
}
