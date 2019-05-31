package ru.romangr.catbot.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.ExecutionResult;
import ru.romangr.exceptional.Exceptional;

class TelegramActionExecutorTest {

  private final RateLimiter rateLimiter = new RateLimiter();

  @Test
  void executeLessThanOneBulk() throws InterruptedException {
    ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
    AtomicReference<Runnable> runnableHolder = new AtomicReference<>();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    given(executorService
        .scheduleWithFixedDelay(notNull(), anyLong(), anyLong(), eq(TimeUnit.SECONDS)))
        .willAnswer(invocation -> {
          runnableHolder.set(invocation.getArgument(0));
          countDownLatch.countDown();
          return mock(ScheduledFuture.class);
        });
    TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
    TelegramActionExecutor actionExecutor
        = new TelegramActionExecutor(executorService, rateLimiter, actionFactory);
    countDownLatch.await();
    ActionsToExecute actionsToExecute = new ActionsToExecute(25);
    AtomicInteger counter = actionsToExecute.getCounter();
    List<TelegramAction> actions = actionsToExecute.getActions();

    actionExecutor.execute(actions);
    runnableHolder.get().run();

    assertThat(counter).hasValue(25);
    verify(executorService).scheduleWithFixedDelay(any(), eq(30L), eq(2L), eq(TimeUnit.SECONDS));
    verifyZeroInteractions(actionFactory);
  }

  @Test
  void executeMoreThanOneBulk() throws InterruptedException {
    ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
    AtomicReference<Runnable> runnableHolder = new AtomicReference<>();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    given(executorService
        .scheduleWithFixedDelay(notNull(), anyLong(), anyLong(), eq(TimeUnit.SECONDS)))
        .willAnswer(invocation -> {
          runnableHolder.set(invocation.getArgument(0));
          countDownLatch.countDown();
          return mock(ScheduledFuture.class);
        });
    TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
    TelegramActionExecutor actionExecutor
        = new TelegramActionExecutor(executorService, rateLimiter, actionFactory);
    countDownLatch.await();
    ActionsToExecute actionsToExecute = new ActionsToExecute(55);
    AtomicInteger counter = actionsToExecute.getCounter();
    List<TelegramAction> actions = actionsToExecute.getActions();

    actionExecutor.execute(actions);
    runnableHolder.get().run();

    assertThat(counter).hasValue(25);

    runnableHolder.get().run();

    assertThat(counter).hasValue(50);

    runnableHolder.get().run();

    assertThat(counter).hasValue(55);
    verifyZeroInteractions(actionFactory);
  }

  @Test
  void handleFailures() throws InterruptedException {
    ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
    AtomicReference<Runnable> runnableHolder = new AtomicReference<>();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    given(executorService
        .scheduleWithFixedDelay(notNull(), anyLong(), anyLong(), eq(TimeUnit.SECONDS)))
        .willAnswer(invocation -> {
          runnableHolder.set(invocation.getArgument(0));
          countDownLatch.countDown();
          return mock(ScheduledFuture.class);
        });
    TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
    TelegramActionExecutor actionExecutor
        = new TelegramActionExecutor(executorService, rateLimiter, actionFactory);
    countDownLatch.await();
    ActionsToExecute actionsToExecute1
        = new ActionsToExecute(15, ExecutionResult.SUCCESS);
    ActionsToExecute actionsToExecute2
        = new ActionsToExecute(5, ExecutionResult.FAILURE);
    ActionsToExecute actionsToExecute3
        = new ActionsToExecute(5, ExecutionResult.SUCCESS);
    List<TelegramAction> actions = new ArrayList<>();
    actions.addAll(actionsToExecute1.getActions());
    actions.addAll(actionsToExecute2.getActions());
    actions.addAll(actionsToExecute3.getActions());

    actionExecutor.execute(actions);
    runnableHolder.get().run();

    int actionsExecuted = actionsToExecute1.getCounter().get()
        + actionsToExecute2.getCounter().get() + actionsToExecute3.getCounter().get();
    assertThat(actionsExecuted).isEqualTo(25);
    verifyZeroInteractions(actionFactory);
  }

  @Test
  void handleRateLimitFailures() throws InterruptedException {
    ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
    AtomicReference<Runnable> runnableHolder = new AtomicReference<>();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    given(executorService
        .scheduleWithFixedDelay(notNull(), anyLong(), anyLong(), eq(TimeUnit.SECONDS)))
        .willAnswer(invocation -> {
          runnableHolder.set(invocation.getArgument(0));
          countDownLatch.countDown();
          return mock(ScheduledFuture.class);
        });
    TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
    TelegramActionExecutor actionExecutor
        = new TelegramActionExecutor(executorService, rateLimiter, actionFactory);
    countDownLatch.await();
    ActionsToExecute actionsToExecute1
        = new ActionsToExecute(19, ExecutionResult.SUCCESS);
    AtomicInteger rateLimitingCounter = new AtomicInteger();
    TelegramAction rateLimitingResultAction = mock(TelegramAction.class);
    given(rateLimitingResultAction.execute()).willAnswer(invocation -> {
      rateLimitingCounter.incrementAndGet();
      return Exceptional.exceptional(
          rateLimitingCounter.get() == 1
              ? ExecutionResult.RATE_LIMIT_FAILURE
              : ExecutionResult.SUCCESS
      );
    });
    given(rateLimitingResultAction.getChat()).willReturn(getChat(getRandomInt()));
    ActionsToExecute actionsToExecute3
        = new ActionsToExecute(5, ExecutionResult.SUCCESS);
    List<TelegramAction> actions = new ArrayList<>(actionsToExecute1.getActions());
    actions.add(rateLimitingResultAction);
    actions.addAll(actionsToExecute3.getActions());

    actionExecutor.execute(actions);
    LocalDateTime beforeExecution = LocalDateTime.now();
    runnableHolder.get().run();
    LocalDateTime afterExecution = LocalDateTime.now();
    runnableHolder.get().run();

    assertThat(Duration.between(beforeExecution, afterExecution))
        .isGreaterThan(Duration.ofMillis(9990));
    assertThat(rateLimitingCounter).hasValue(2);
    int actionsExecuted = actionsToExecute1.getCounter().get()
        + rateLimitingCounter.get() + actionsToExecute3.getCounter().get();
    assertThat(actionsExecuted).isEqualTo(26);
    verifyZeroInteractions(actionFactory);
  }

  @Test
  void rateLimitForUser() throws InterruptedException {
    ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
    AtomicReference<Runnable> runnableHolder = new AtomicReference<>();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    given(executorService
        .scheduleWithFixedDelay(notNull(), anyLong(), anyLong(), eq(TimeUnit.SECONDS)))
        .willAnswer(invocation -> {
          runnableHolder.set(invocation.getArgument(0));
          countDownLatch.countDown();
          return mock(ScheduledFuture.class);
        });
    TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
    TelegramAction tooManyRequestsMessageAction = mock(TelegramAction.class);
    given(actionFactory.newSendMessageAction(any(), any()))
        .willReturn(tooManyRequestsMessageAction);
    given(tooManyRequestsMessageAction.execute())
        .willReturn(Exceptional.exceptional(ExecutionResult.SUCCESS));
    given(tooManyRequestsMessageAction.getChat()).willReturn(getChat(100001));
    TelegramActionExecutor actionExecutor
        = new TelegramActionExecutor(executorService, rateLimiter, actionFactory);
    countDownLatch.await();

    ActionsToExecute activeUserActions
        = new ActionsToExecute(25, ExecutionResult.SUCCESS, () -> 100001);
    List<TelegramAction> actions = new ArrayList<>(activeUserActions.getActions());

    actionExecutor.execute(actions);

    runnableHolder.get().run();
    assertThat(activeUserActions.getCounter()).hasValue(20);
    verify(tooManyRequestsMessageAction).execute();

    runnableHolder.get().run();
    assertThat(activeUserActions.getCounter()).hasValue(20);
  }

  private static class ActionsToExecute {

    private final int numberOfActions;
    private final ExecutionResult executionResult;
    private final Supplier<Integer> chatIdSupplier;
    private AtomicInteger counter;
    private List<TelegramAction> actions;

    private ActionsToExecute(int numberOfActions) {
      this(numberOfActions, ExecutionResult.SUCCESS);
    }

    private ActionsToExecute(int numberOfActions, ExecutionResult executionResult) {
      this(numberOfActions, executionResult, TelegramActionExecutorTest::getRandomInt);
    }

    private ActionsToExecute(int numberOfActions, ExecutionResult executionResult,
        Supplier<Integer> chatIdSupplier) {
      this.numberOfActions = numberOfActions;
      this.executionResult = executionResult;
      this.chatIdSupplier = chatIdSupplier;
      this.invoke();
    }

    AtomicInteger getCounter() {
      return counter;
    }

    List<TelegramAction> getActions() {
      return actions;
    }

    private void invoke() {
      counter = new AtomicInteger();
      actions = IntStream.range(0, numberOfActions)
          .mapToObj(operand -> mock(TelegramAction.class))
          .peek(telegramAction -> {
            given(telegramAction.execute()).willAnswer(invocation -> {
              counter.incrementAndGet();
              return Exceptional.exceptional(executionResult);
            });
            given(telegramAction.getChat()).willReturn(getChat(chatIdSupplier.get()));
          })
          .collect(Collectors.toList());
    }
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
