package ru.romangr.catbot.executor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.catbot.telegram.model.Chat;

@Slf4j
public class RateLimiter {

  private static final int CHAT_ACTIONS_PER_MINUTE_LIMIT = 20;

  private final LoadingCache<Integer, AtomicInteger> chats;
  private final Map<Integer, Boolean> chatsToSkip;

  public RateLimiter() {
    this(Duration.ofMinutes(1), Duration.ofMinutes(1));
  }

  RateLimiter(Duration limitPeriod, Duration banTimeout) {
    this.chats = CacheBuilder.newBuilder()
        .expireAfterWrite(limitPeriod)
        .maximumSize(1000)
        .build(CacheLoader.from(key -> new AtomicInteger()));

    this.chatsToSkip = CacheBuilder.newBuilder()
        .expireAfterAccess(banTimeout)
        .maximumSize(1000)
        .build(CacheLoader.<Integer, Boolean>from(key -> {
          throw new RuntimeException("Should not be used");
        }))
        .asMap();
  }

  @SneakyThrows
  public RateLimitResult check(Chat chat) {
    int actionsCount = chats.get(chat.getId()).incrementAndGet();
    if (chatsToSkip.getOrDefault(chat.getId(), false)) {
      return RateLimitResult.BANNED;
    }
    if (actionsCount > CHAT_ACTIONS_PER_MINUTE_LIMIT) {
      chatsToSkip.put(chat.getId(), true);
      log.warn("Chat {} with id {} has been banned because of too many actions",
          getChatName(chat), chat.getId());
      return RateLimitResult.MADE_BANNED;
    }
    return RateLimitResult.POSITIVE;
  }

  private String getChatName(Chat chat) {
    return Stream.of(chat.getFirstName(), chat.getLastName(), chat.getTitle(), chat.getUsername())
        .filter(Objects::nonNull)
        .findFirst()
        .map(name -> "'" + name + "'")
        .orElse("");
  }

}
