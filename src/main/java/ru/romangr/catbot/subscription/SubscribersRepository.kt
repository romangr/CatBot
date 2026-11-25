package ru.romangr.catbot.subscription

import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import ru.romangr.catbot.telegram.model.Chat
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * Roman 11.05.2017.
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
class SubscribersRepository(subscribersFile: Path?) {
    private val subscribersFile: Path
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
        log.debug("Deleting subscriber {}", subscriber)
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
                json.decodeFromStream(SUBSCRIBERS_SERIALIZER, inputStream)
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
            Files.newOutputStream(subscribersFile)
                .use { outputStream -> json.encodeToStream(SUBSCRIBERS_SERIALIZER, subscribers, outputStream) }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    companion object {
        private val SUBSCRIBERS_SERIALIZER = SetSerializer(Chat.serializer())
        private val json = Json { ignoreUnknownKeys = true }
        private val DEFAULT_SUBSCRIBERS_FILE = Paths.get("subscribers.json")
        private val log = org.slf4j.LoggerFactory.getLogger(SubscribersRepository::class.java)
    }
}
