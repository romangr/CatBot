package ru.romangr.catbot.executor;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.ExecutionResult;

@Slf4j
@RequiredArgsConstructor
public class TelegramActionExecutor {

  private static final int ACTIONS_TO_EXECUTE_PER_BULK = 25;
  private static final int RATE_LIMIT_AVOID_TIMEOUT_SECONDS = 10;
  private static final String TOO_MANY_REQUESTS_MESSAGE = "You are sending too many requests";

  private final RateLimiter rateLimiter;
  private final TelegramActionFactory actionFactory;
  private final Queue<TelegramAction> actionsQueue = new ConcurrentLinkedQueue<>();

  public TelegramActionExecutor(ScheduledExecutorService executor, RateLimiter rateLimiter,
      TelegramActionFactory actionFactory) {
    this.rateLimiter = rateLimiter;
    this.actionFactory = actionFactory;
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
      if (rateLimitResult == RateLimitResult.BANNED
          || rateLimitResult == RateLimitResult.MADE_BANNED) {
        log.trace("Skipping action for banned user");
        if (rateLimitResult == RateLimitResult.MADE_BANNED) {
          actionFactory.newSendMessageAction(chat, TOO_MANY_REQUESTS_MESSAGE).execute()
              .ifException(e -> log.warn("Exception during sending rate limit message", e));
        }
        i--;
        continue;
      }
      ExecutionResult executionResult = action.execute()
          .ifException(e -> log.warn("Exception during action execution", e))
          .getOrDefault(ExecutionResult.FAILURE);
      if (executionResult == ExecutionResult.RATE_LIMIT_FAILURE) {
        log.warn("Telegram rate limit error during action execution");
        Thread.sleep(RATE_LIMIT_AVOID_TIMEOUT_SECONDS * 1000);
        actionsQueue.add(action);
        break;
      }
    }
  }

}
