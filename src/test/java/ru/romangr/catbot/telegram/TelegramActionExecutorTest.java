package ru.romangr.catbot.telegram;

import org.junit.jupiter.api.Test;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.catbot.handler.action.TelegramAction;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TelegramActionExecutorTest {

    @Test
    void executeLessThanOneBulk() throws InterruptedException {
        ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
        AtomicReference<Runnable> runnableHolder = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        given(executorService.scheduleWithFixedDelay(notNull(), anyLong(), anyLong(), eq(TimeUnit.SECONDS)))
                .willAnswer(invocation -> {
                    runnableHolder.set(invocation.getArgument(0));
                    countDownLatch.countDown();
                    return mock(ScheduledFuture.class);
                });
        TelegramActionExecutor actionExecutor = new TelegramActionExecutor(executorService);
        countDownLatch.await();
        ActionsToExecute actionsToExecute = new ActionsToExecute(25).invoke();
        AtomicInteger counter = actionsToExecute.getCounter();
        List<TelegramAction> actions = actionsToExecute.getActions();

        actionExecutor.execute(actions);
        runnableHolder.get().run();

        assertThat(counter).hasValue(25);
        verify(executorService).scheduleWithFixedDelay(any(), eq(30L), eq(65L), eq(TimeUnit.SECONDS));
    }

    @Test
    void executeMoreThanOneBulk() throws InterruptedException {
        ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
        AtomicReference<Runnable> runnableHolder = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        given(executorService.scheduleWithFixedDelay(notNull(), anyLong(), anyLong(), eq(TimeUnit.SECONDS)))
                .willAnswer(invocation -> {
                    runnableHolder.set(invocation.getArgument(0));
                    countDownLatch.countDown();
                    return mock(ScheduledFuture.class);
                });
        TelegramActionExecutor actionExecutor = new TelegramActionExecutor(executorService);
        countDownLatch.await();
        ActionsToExecute actionsToExecute = new ActionsToExecute(55).invoke();
        AtomicInteger counter = actionsToExecute.getCounter();
        List<TelegramAction> actions = actionsToExecute.getActions();

        actionExecutor.execute(actions);
        runnableHolder.get().run();

        assertThat(counter).hasValue(25);

        runnableHolder.get().run();

        assertThat(counter).hasValue(50);

        runnableHolder.get().run();

        assertThat(counter).hasValue(55);
    }

    private class ActionsToExecute {

        private final int numberOfActions;
        private AtomicInteger counter;
        private List<TelegramAction> actions;

        private ActionsToExecute(int numberOfActions) {
            this.numberOfActions = numberOfActions;
        }

        AtomicInteger getCounter() {
            return counter;
        }

        List<TelegramAction> getActions() {
            return actions;
        }

        ActionsToExecute invoke() {
            counter = new AtomicInteger();
            actions = IntStream.range(0, numberOfActions)
                    .mapToObj(operand -> mock(TelegramAction.class))
                    .peek(telegramAction -> {
                        given(telegramAction.execute()).willAnswer(invocation -> {
                            counter.incrementAndGet();
                            return Exceptional.exceptional(new Object());
                        });
                    })
                    .collect(Collectors.toList());
            return this;
        }
    }
}
