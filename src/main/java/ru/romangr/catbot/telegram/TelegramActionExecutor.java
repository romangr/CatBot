package ru.romangr.catbot.telegram;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.catbot.handler.action.TelegramAction;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ru.romangr.catbot.telegram.model.ExecutionResult;

@Slf4j
public class TelegramActionExecutor {

  private static final int ACTIONS_TO_EXECUTE_PER_BULK = 25;
  private static final int RATE_LIMIT_AVOID_TIMEOUT_SECONDS = 10;

  private final Queue<TelegramAction> actionsQueue = new ConcurrentLinkedQueue<>();

  public TelegramActionExecutor(ScheduledExecutorService executor) {
    executor.scheduleWithFixedDelay(this::execute, 30, 2, TimeUnit.SECONDS);
  }

  public void execute(List<TelegramAction> actions) {
    actionsQueue.addAll(actions);
  }

  @SneakyThrows
  private void execute() {
    log.debug("Executing a bulk of Telegram actions");
    for (int i = 0; i < ACTIONS_TO_EXECUTE_PER_BULK; i++) {
      TelegramAction action = actionsQueue.poll();
      if (action == null) {
        break;
      }
      ExecutionResult executionResult = action.execute()
          .ifException(e -> log.warn("Exception during action execution", e))
          .getOrDefault(ExecutionResult.FAILURE);
      if (executionResult == ExecutionResult.RATE_LIMIT_FAILURE) {
        actionsQueue.add(action);
        Thread.sleep(RATE_LIMIT_AVOID_TIMEOUT_SECONDS * 1000);
        break;
      }
    }
  }
}
