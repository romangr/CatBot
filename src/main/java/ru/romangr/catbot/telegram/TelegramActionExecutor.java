package ru.romangr.catbot.telegram;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.catbot.handler.action.TelegramAction;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.ExecutionResult;

@Slf4j
public class TelegramActionExecutor {

  private static final int ACTIONS_TO_EXECUTE_PER_BULK = 25;
  private static final int RATE_LIMIT_AVOID_TIMEOUT_SECONDS = 10;
  private final LoadingCache<Integer, AtomicInteger> chats
      = CacheBuilder.newBuilder()
      .expireAfterAccess(1, TimeUnit.MINUTES)
      .maximumSize(1000)
      .build(CacheLoader.from(key -> new AtomicInteger()));
  private final Map<Integer, Boolean> chatsToSkip
      = CacheBuilder.newBuilder()
      .expireAfterAccess(1, TimeUnit.MINUTES)
      .maximumSize(1000)
      .build(CacheLoader.<Integer, Boolean>from(key -> {
        throw new RuntimeException("Should not be used");
      }))
      .asMap();

  private final Queue<TelegramAction> actionsQueue = new ConcurrentLinkedQueue<>();

  public TelegramActionExecutor(ScheduledExecutorService executor) {
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
      int chatId = chat.getId();
      if (chatsToSkip.getOrDefault(chatId, false)) {
        i--;
        continue;
      }
      int chatActionsCount = chats.get(chatId).getAndIncrement();
      ExecutionResult executionResult = action.execute()
          .ifException(e -> log.warn("Exception during action execution", e))
          .getOrDefault(ExecutionResult.FAILURE);
      if (executionResult == ExecutionResult.RATE_LIMIT_FAILURE) {
        log.warn("Telegram rate limit error during action execution");
        Thread.sleep(RATE_LIMIT_AVOID_TIMEOUT_SECONDS * 1000);
        if (chatActionsCount > 20) {
          log.warn("Chat {} with id {} has been banned because of too many actions",
              getChatName(chat), chatId);
          chatsToSkip.put(chatId, true);
          break;
        }
        actionsQueue.add(action);
        break;
      }
    }
  }

  private String getChatName(Chat chat) {
    return Stream.of(chat.getFirstName(), chat.getLastName(), chat.getTitle(), chat.getUsername())
        .filter(Objects::nonNull)
        .findFirst()
        .map(name -> "'" + name + "'")
        .orElse("");
  }
}
