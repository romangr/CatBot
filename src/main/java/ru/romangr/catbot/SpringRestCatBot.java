package ru.romangr.catbot;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import ru.romangr.catbot.executor.TelegramActionExecutor;
import ru.romangr.catbot.handler.UpdatesHandler;
import ru.romangr.catbot.subscription.SubscribersService;
import ru.romangr.catbot.telegram.TelegramAdminNotifier;
import ru.romangr.catbot.telegram.TelegramRequestExecutor;
import ru.romangr.catbot.telegram.model.Update;
import ru.romangr.catbot.utils.DelayCalculator;
import ru.romangr.catbot.utils.PropertiesResolver;
import ru.romangr.exceptional.Exceptional;

/**
 * Roman 27.10.2016.
 */
@Slf4j
@RequiredArgsConstructor
public class SpringRestCatBot implements RestBot {

  private static final int MAX_CONSECUTIVE_ERRORS_BEFORE_DELAY = 3;

  private final AtomicBoolean wereIssuesDuringSendingToSubscribers = new AtomicBoolean(false);
  private final UpdatesHandler updatesHandler;
  private final SubscribersService subscribersService;
  private final int updatesCheckPeriod;
  private final ScheduledExecutorService updatesReceivingExecutorService
      = Executors.newSingleThreadScheduledExecutor();
  private final ScheduledExecutorService subscriptionExecutorService
      = Executors.newSingleThreadScheduledExecutor();
  private final ScheduledExecutorService heartbeatExecutorService
      = Executors.newSingleThreadScheduledExecutor();
  private final TelegramRequestExecutor requestExecutor;
  private final TelegramActionExecutor actionExecutor;
  private final PropertiesResolver propertiesResolver;
  private volatile long currentUpdateOffset = 0;
  private final TelegramAdminNotifier adminNotifier;
  private final AtomicInteger updatesCheckCounter = new AtomicInteger();
  private volatile int previousUpdatesCheckCounterValue = 0;
  private final AtomicBoolean sendingToSubscribersInProgress = new AtomicBoolean(false);
  private volatile Instant delayUpdatesRequestUntil = Instant.MIN;
  private final AtomicInteger consecutiveErrors = new AtomicInteger();

  private void processUpdates(Exceptional<List<Update>> updatesExceptional) {
    updatesCheckCounter.incrementAndGet();
    if (Instant.now().isBefore(delayUpdatesRequestUntil)) {
      log.warn("Updates check is delayed until {}, skipping processing", delayUpdatesRequestUntil.toString());
      return;
    }
    updatesExceptional
        .ifValue(u -> consecutiveErrors.set(0))
        .handleException(e -> {
          int consecutiveErrors = this.consecutiveErrors.incrementAndGet();
          log.warn("Error getting updates from Telegram API, this is a consecutive error #{}",
              consecutiveErrors, e);
          if (consecutiveErrors > MAX_CONSECUTIVE_ERRORS_BEFORE_DELAY) {
            log.warn("More than {} consecutive errors, delaying the execution...", MAX_CONSECUTIVE_ERRORS_BEFORE_DELAY);
            Duration delay = Duration.ofMinutes(2L * consecutiveErrors);
            log.warn("Delaying updates processing for {} minutes", delay.get(ChronoUnit.MINUTES));
            this.delayUpdatesRequestUntil = Instant.now().plus(delay);
          }
        })
        .handleException(e -> log.warn("Exception while processing Telegram API error", e))
        .ifValue(this::processUpdates);
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
    adminNotifier.botStarted(subscribersService.getSubscribersCount());
    updatesReceivingExecutorService.scheduleAtFixedRate(
        () -> this.processUpdates(this.getUpdates()), 0, updatesCheckPeriod, TimeUnit.SECONDS);
    Duration delay = DelayCalculator.calculateDelayToRunAtParticularTime(
        propertiesResolver.getTimeToSendMessageToSubscribers());
    log.info("Next sending to subscribers in {} minutes", delay.getSeconds() / 60);
    subscriptionExecutorService
        .scheduleAtFixedRate(this::sendMessageToSubscribers, delay.getSeconds(),
            DelayCalculator.getSecondsFromHours(24), TimeUnit.SECONDS);
    heartbeatExecutorService.scheduleAtFixedRate(() -> {
      int updatesCheckCounterValue = updatesCheckCounter.get();
      if (wereIssuesDuringSendingToSubscribers.get()) {
        sendMessageToSubscribers();
      }
      if (updatesCheckCounterValue == previousUpdatesCheckCounterValue && updatesCheckCounterValue != 0) {
        log.warn("Updates check counter value seems to be stuck at {}!", updatesCheckCounterValue);
      } else {
        log.info("All systems are fine! Updates check counter: {}", updatesCheckCounterValue);
      }
      previousUpdatesCheckCounterValue = updatesCheckCounterValue;
    }, 1, 1, TimeUnit.HOURS);
  }

  private Exceptional<List<Update>> getUpdates() {
    return requestExecutor.getUpdates(currentUpdateOffset)
        .ifValue(updates -> updates.stream()
            .mapToLong(Update::getId)
            .max()
            .ifPresent(maxUpdateId -> currentUpdateOffset = maxUpdateId + 1)
        );
  }

  private void sendMessageToSubscribers() {
    boolean processIsNotRunning = sendingToSubscribersInProgress.compareAndSet(false, true);
    if (!processIsNotRunning) {
      log.warn("Sending to subscribers process is already running, skipping the execution");
      return;
    }
    this.subscribersService.sendMessageToSubscribers()
        .ifValue(v -> {
          this.wereIssuesDuringSendingToSubscribers.set(false);
          actionExecutor.execute(v);
        })
        .ifException(e -> {
          this.wereIssuesDuringSendingToSubscribers.set(true);
          log.warn("Failed to send message to subscribers", e);
        });
    sendingToSubscribersInProgress.set(false);
  }

}
