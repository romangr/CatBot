package ru.romangr.catbot.telegram;

import lombok.extern.slf4j.Slf4j;
import ru.romangr.catbot.handler.action.TelegramAction;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TelegramActionExecutor {

    private static final int ACTIONS_TO_EXECUTE_PER_BULK = 25;

    private final Queue<TelegramAction> actionsQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executor;

    public TelegramActionExecutor(ScheduledExecutorService executor) {
        this.executor = executor;
        this.executor.scheduleWithFixedDelay(this::execute, 30, 65, TimeUnit.SECONDS);
    }

    public void execute(List<TelegramAction> actions) {
        actionsQueue.addAll(actions);
    }

    private void execute() {
        log.debug("Executing a bulk of Telegram actions");
        for (int i = 0; i < ACTIONS_TO_EXECUTE_PER_BULK; i++) {
            TelegramAction action = actionsQueue.poll();
            if (action == null) {
                break;
            }
            // todo: retry for 429 status
            action.execute()
                    .ifException(e -> log.warn("Exception during action execution", e));
        }
    }
}
