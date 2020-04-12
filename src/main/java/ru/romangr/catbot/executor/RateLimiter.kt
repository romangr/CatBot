package ru.romangr.catbot.executor

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.slf4j.LoggerFactory
import ru.romangr.catbot.telegram.model.Chat
import java.time.Duration
import java.util.Objects
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream

class RateLimiter internal constructor(limitPeriod: Duration, banTimeout: Duration) {
    private val chats: LoadingCache<Int, AtomicInteger> = CacheBuilder.newBuilder()
            .expireAfterWrite(limitPeriod)
            .maximumSize(1000)
            .build(CacheLoader.from { _: Int? -> AtomicInteger() })

    private val chatsToSkip: MutableMap<Int, Boolean> = CacheBuilder.newBuilder()
            .expireAfterAccess(banTimeout)
            .maximumSize(1000)
            .build(CacheLoader.from<Int, Boolean> {
                throw RuntimeException("Should not be used")
            })
            .asMap()

    constructor() : this(Duration.ofMinutes(1), Duration.ofMinutes(1))

    fun check(chat: Chat): RateLimitResult {
        val actionsCount: Int
        try {
            actionsCount = chats[chat.id].incrementAndGet()
        } catch (e: ExecutionException) {
            log.warn("Error incrementing rate limiter counter", e.cause)
            return RateLimitResult.BANNED
        }
        if (chatsToSkip.getOrDefault(chat.id, false)) {
            return RateLimitResult.BANNED
        }
        if (actionsCount > CHAT_ACTIONS_PER_MINUTE_LIMIT) {
            chatsToSkip[chat.id] = true
            log.warn("Chat {} with id {} has been banned because of too many actions",
                    getChatName(chat), chat.id)
            return RateLimitResult.MADE_BANNED
        }
        return RateLimitResult.POSITIVE
    }

    private fun getChatName(chat: Chat): String {
        return Stream.of(chat.firstName, chat.lastName, chat.title, chat.username)
                .filter { obj: String? -> Objects.nonNull(obj) }
                .findFirst()
                .map { name: String? -> "'$name'" }
                .orElse("")
    }

    companion object {
        private val log = LoggerFactory.getLogger(RateLimiter::class.java)
        private const val CHAT_ACTIONS_PER_MINUTE_LIMIT = 20
    }
}
