package ru.romangr.catbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import ru.romangr.catbot.utils.PropertiesResolver;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.catbot.handler.UpdatesHandler;
import ru.romangr.catbot.subscription.SubscribersService;
import ru.romangr.catbot.telegram.TelegramActionExecutor;
import ru.romangr.catbot.telegram.TelegramRequestExecutor;
import ru.romangr.catbot.telegram.model.Update;
import ru.romangr.catbot.utils.DelayCalculator;

import java.time.Duration;
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
public class SpringRestCatBot implements RestBot {

    private final AtomicBoolean wereIssuesDuringSendingToSubscribers = new AtomicBoolean(false);
    private final UpdatesHandler updatesHandler;
    private final SubscribersService subscribersService;
    private final int updatesCheckPeriod;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final TelegramRequestExecutor requestExecutor;
    private int currentUpdateOffset = 0;
    private final TelegramActionExecutor actionExecutor;
    private final PropertiesResolver propertiesResolver;

    private void processUpdates(Exceptional<List<Update>> updatesExceptional) {
        updatesExceptional
                .ifException(e -> log.warn("Error getting updates from Telegram API", e))
                .ifValue(this::processUpdates)
                .ifException(e -> log.warn("Exception during processing updates", e));
    }

    private void processUpdates(List<Update> updates) {
        if (CollectionUtils.isEmpty(updates)) {
            return;
        }
        for (Update update : updates) {
            updatesHandler.handleUpdate(update);
        }
    }

    @Override
    public void start() {
        log.info("Bot started! Total subscribers: {}", subscribersService.getSubscribersCount());
        executorService.scheduleAtFixedRate(
                () -> this.processUpdates(this.getUpdates()), 0, updatesCheckPeriod, TimeUnit.SECONDS);
        Duration delay = DelayCalculator.calculateDelayToRunAtParticularTime(
                propertiesResolver.getTimeToSendMessageToSubscribers());
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
                .ifValue(updates -> updates.stream()
                        .mapToInt(Update::getId)
                        .max()
                        .ifPresent(maxUpdateId -> currentUpdateOffset = maxUpdateId + 1)
                );
    }

    private void sendMessageToSubscribers() {
        this.subscribersService.sendMessageToSubscribers()
                .ifValue(v -> {
                    this.wereIssuesDuringSendingToSubscribers.set(false);
                    actionExecutor.execute(v);
                })
                .ifException(v -> this.wereIssuesDuringSendingToSubscribers.set(true));
    }

}
