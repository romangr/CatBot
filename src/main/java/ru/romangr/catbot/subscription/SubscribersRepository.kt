package ru.romangr.catbot.subscription

import com.fasterxml.jackson.databind.ObjectMapper
import ru.romangr.catbot.telegram.model.Chat
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * Roman 11.05.2017.
 */
class SubscribersRepository(subscribersFile: Path?) {
    private val subscribersFile: Path
    private val objectMapper = ObjectMapper()
    private val subscribersTypeToken = objectMapper.typeFactory
            .constructCollectionType(Set::class.java, Chat::class.java)
    private val subscribers: MutableSet<Chat> = HashSet()

    val allSubscribers: Collection<Chat>
        get() = subscribers

    val subscribersCount: Int
        get() = subscribers.size

    init {
        if (subscribersFile == null) {
            this.subscribersFile = DEFAULT_SUBSCRIBERS_FILE
        } else {
            this.subscribersFile = subscribersFile
        }
        refreshSubscribersFromFile()
    }

    fun addSubscriber(subscriber: Chat): Boolean {
        val isSubscriberAdded = subscribers.add(subscriber)
        if (isSubscriberAdded) {
            saveSubscribersToFile()
        }
        return isSubscriberAdded
    }

    fun deleteSubscriber(subscriber: Chat): Boolean {
        val isSubscriberDeleted = subscribers.remove(subscriber)
        if (isSubscriberDeleted) {
            saveSubscribersToFile()
        }
        return isSubscriberDeleted
    }

    @Synchronized
    private fun refreshSubscribersFromFile() {
        if (!Files.exists(subscribersFile)) {
            return
        }
        try {
            Files.newInputStream(subscribersFile).use { inputStream ->
                objectMapper.readValue<Set<Chat>>(inputStream, subscribersTypeToken)
            }.let { subscribersFromFile ->
                subscribers.clear()
                subscribers.addAll(subscribersFromFile)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    @Synchronized
    private fun saveSubscribersToFile() {
        try {
            Files.newOutputStream(subscribersFile).use { outputStream -> objectMapper.writeValue(outputStream, subscribers) }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    companion object {
        private val DEFAULT_SUBSCRIBERS_FILE = Paths.get("subscribers.json")
        private val log = org.slf4j.LoggerFactory.getLogger(SubscribersRepository::class.java)
    }
}
