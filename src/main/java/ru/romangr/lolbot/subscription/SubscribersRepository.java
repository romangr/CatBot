package ru.romangr.lolbot.subscription;

import ru.romangr.lolbot.telegram.model.Chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Roman 11.05.2017.
 */
public class SubscribersRepository {

  private static final Logger LOGGER = Logger.getLogger(SubscribersRepository.class.getName());

  private final static Path DEFAULT_SUBSCRIBERS_FILE = Paths.get("subscribers.json");
  private final Path subscribersFile;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final CollectionType subscribersTypeToken = objectMapper.getTypeFactory()
      .constructCollectionType(Set.class, Chat.class);
  private Set<Chat> subscribers = new HashSet<>();

  public SubscribersRepository(Path subscribersFile) {
    if (subscribersFile == null) {
      this.subscribersFile = DEFAULT_SUBSCRIBERS_FILE;
    } else {
      this.subscribersFile = subscribersFile;
    }
    refreshSubscribersFromFile();
  }

  public Collection<Chat> getAllSubscribers() {
    return subscribers;
  }

  public int getSubscribersCount() {
    return subscribers.size();
  }

  public boolean addSubscriber(Chat subscriber) {
    boolean isSubscriberAdded = subscribers.add(subscriber);
    if (isSubscriberAdded) {
      saveSubscribersToFile();
    }
    return isSubscriberAdded;
  }

  public boolean deleteSubscriber(Chat subscriber) {
    boolean isSubscriberDeleted = subscribers.remove(subscriber);
    if (isSubscriberDeleted) {
      saveSubscribersToFile();
    }
    return isSubscriberDeleted;
  }

  private synchronized void refreshSubscribersFromFile() {
    if (!Files.exists(subscribersFile)) {
      return;
    }
    try (InputStream inputStream = Files.newInputStream(subscribersFile)) {
      subscribers = objectMapper.readValue(inputStream, subscribersTypeToken);
    } catch (IOException e) {
      LOGGER.warning(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private synchronized void saveSubscribersToFile() {
    try (OutputStream outputStream = Files.newOutputStream(subscribersFile)) {
      objectMapper.writeValue(outputStream, subscribers);
    } catch (IOException e) {
      LOGGER.warning(e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
