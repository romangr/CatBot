package Subscription;

import Model.Chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    private final static Path DEFAULT_SUBSCRIBERS_FILE = Paths.get("subscribers.data");
    private final Path subscribersFile;
    private Set<Chat> subscribers = new HashSet<>();

    public SubscribersRepository(Path subscribersFile) {
        if (subscribersFile == null) {
            this.subscribersFile = DEFAULT_SUBSCRIBERS_FILE;
            return;
        }
        this.subscribersFile = subscribersFile;
        refreshSubscribersFromFile();
    }

    public Collection<Chat> getAllSubscribers() {
        return subscribers;
    }

    public int getSubscribersCount() {
        return subscribers.size();
    }

    public boolean addSubscriber(Chat subscriber) {
        boolean addResult = subscribers.add(subscriber);
        saveSubscribersToFile();
        return addResult;
    }

    public boolean deleteSubscriber(Chat subscriber) {
        boolean removeResult = subscribers.remove(subscriber);
        if (removeResult) {
            saveSubscribersToFile();
        }
        return removeResult;
    }

    @SuppressWarnings("unchecked")
    private synchronized void refreshSubscribersFromFile() {
        if (!Files.exists(subscribersFile)) {
            return;
        }
        try (InputStream inputStream = Files.newInputStream(subscribersFile);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            subscribers = (Set<Chat>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warning(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private synchronized void saveSubscribersToFile() {
        try (OutputStream inputStream = Files.newOutputStream(subscribersFile);
             ObjectOutputStream objectInputStream = new ObjectOutputStream(inputStream)) {
            objectInputStream.writeObject(subscribers);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
