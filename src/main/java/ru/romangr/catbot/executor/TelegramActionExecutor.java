package ru.romangr.catbot.executor;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.ExecutionResult;

@Slf4j
@RequiredArgsConstructor
public class TelegramActionExecutor {

  private static final int ACTIONS_TO_EXECUTE_PER_BULK = 25;
  private static final int RATE_LIMIT_AVOID_TIMEOUT_SECONDS = 10;

  private final RateLimiter rateLimiter;
  private final Queue<TelegramAction> actionsQueue = new ConcurrentLinkedQueue<>();

  public TelegramActionExecutor(ScheduledExecutorService executor, RateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
    executor.scheduleWithFixedDelay(this::execute, 30, 2, TimeUnit.SECONDS);
  }

  public void execute(List<TelegramAction> actions) {
    actionsQueue.addAll(actions);
  }

  @SneakyThrows
  private void execute() {
    log.trace("Executing a bulk of Telegram actions");
    for (int i = 0; i < ACTIONS_TO_EXECUTE_PER_BULK; i++) {
      TelegramAction action = actionsQueue.poll();
      if (action == null) {
        break;
      }
      Chat chat = action.getChat();
      RateLimitResult rateLimitResult = rateLimiter.check(chat);
      if (rateLimitResult == RateLimitResult.BANNED) {
        log.trace("Skipping action for banned user");
        i--;
        continue;
      }
      ExecutionResult executionResult = action.execute()
          .ifException(e -> log.warn("Exception during action execution", e))
          .getOrDefault(ExecutionResult.FAILURE);
      if (executionResult == ExecutionResult.RATE_LIMIT_FAILURE) {
        handleRateLimitingFailure(action, rateLimitResult);
        break;
      }
    }
  }

  private void handleRateLimitingFailure(TelegramAction action, RateLimitResult rateLimitResult)
      throws InterruptedException {
    log.warn("Telegram rate limit error during action execution");
    Thread.sleep(RATE_LIMIT_AVOID_TIMEOUT_SECONDS * 1000);
    Chat chat = action.getChat();
    if (rateLimitResult == RateLimitResult.TO_BAN) {
      log.warn("Chat {} with id {} has been banned because of too many actions",
          getChatName(chat), chat.getId());
      rateLimiter.ban(chat);
      return;
    }
    actionsQueue.add(action);
  }

  private String getChatName(Chat chat) {
    return Stream.of(chat.getFirstName(), chat.getLastName(), chat.getTitle(), chat.getUsername())
        .filter(Objects::nonNull)
        .findFirst()
        .map(name -> "'" + name + "'")
        .orElse("");
  }
}
