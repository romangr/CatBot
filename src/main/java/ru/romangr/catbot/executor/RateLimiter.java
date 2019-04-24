package ru.romangr.catbot.executor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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

  public RateLimitResult check(Chat chat) {
    if (chatsToSkip.getOrDefault(chat.getId(), false)) {
      incrementAndGetCounterForChat(chat);
      return RateLimitResult.BANNED;
    }
    if (incrementAndGetCounterForChat(chat) > CHAT_ACTIONS_PER_MINUTE_LIMIT) {
      return RateLimitResult.TO_BAN;
    }
    return RateLimitResult.POSITIVE;
  }

  public void ban(Chat chat) {
    chatsToSkip.put(chat.getId(), true);
  }

  @SneakyThrows
  private int incrementAndGetCounterForChat(Chat chat) {
    return chats.get(chat.getId()).incrementAndGet();
  }
}
