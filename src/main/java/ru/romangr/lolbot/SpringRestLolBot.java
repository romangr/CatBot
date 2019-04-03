package ru.romangr.lolbot;

import ru.romangr.lolbot.telegram.model.Update;
import ru.romangr.lolbot.subscription.SubscribersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.lolbot.telegram.TelegramRequestExecutor;
import ru.romangr.lolbot.utils.DelayCalculator;
import ru.romangr.exceptional.Exceptional;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Roman 27.10.2016.
 */
@Slf4j
@RequiredArgsConstructor
public class SpringRestLolBot implements RestBot {
    private static final int TIME_OCLOCK_TO_SEND_MESSAGE_TO_SUBSCRIBERS = 20;

    private final AtomicBoolean wereIssuesDuringSendingToSubscribers = new AtomicBoolean(false);
    private final UpdatesHandler updatesHandler;
    private final SubscribersService subscribersService;
    private final int updatesCheckPeriod;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final TelegramRequestExecutor requestExecutor;
    private int currentUpdateOffset = 0;

    private void processUpdates(Exceptional<List<Update>> updatesExceptional) {
        updatesExceptional
                .ifException(e -> log.warn("Error getting updates from Telegram API", e))
                .ifValue(this::processUpdates)
                .ifException(e -> log.warn("Exception during processing updates", e));
    }

    private void processUpdates(List<Update> updates) {
        if (updates.isEmpty()) {
            return;
        }
        StringBuilder logString = new StringBuilder();
        logString.append(new Date()).append(": ").append(updates.size()).append(" updates received from: ");
        boolean commandsReceived = false;
        for (Update update : updates) {
            commandsReceived = updatesHandler.handleUpdate(logString, commandsReceived, update);
        }
        if (commandsReceived) {
            log.info(logString.toString());
        }
    }

    @Override
    public void start() {
        log.info("Bot started! Total subscribers: {}", subscribersService.getSubscribersCount());
        executorService.scheduleAtFixedRate(
                () -> this.processUpdates(this.getUpdates()), 0, updatesCheckPeriod, TimeUnit.SECONDS);
        Duration delay = DelayCalculator
                .calculateDelayToRunAtParticularTime(TIME_OCLOCK_TO_SEND_MESSAGE_TO_SUBSCRIBERS);
        log.info("Next sending to subscribers in {} minutes", delay.getSeconds() / 60);
        executorService.scheduleAtFixedRate(this::sendMessageToSubscribers, delay.getSeconds(),
                DelayCalculator.getSecondsFromHours(24), TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(() -> {
            log.info("All systems are fine!");
            if (wereIssuesDuringSendingToSubscribers.get()) {
                sendMessageToSubscribers();
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    private Exceptional<List<Update>> getUpdates() {
        return requestExecutor.getUpdates(currentUpdateOffset)
                .map(updates -> {
                    updates.stream().mapToInt(Update::getId).max()
                            .ifPresent(maxUpdateId -> currentUpdateOffset = maxUpdateId + 1);
                    return updates;
                });
    }

    private void sendMessageToSubscribers() {
        this.wereIssuesDuringSendingToSubscribers
                .set(this.subscribersService.sendMessageToSubscribers());
    }

}
