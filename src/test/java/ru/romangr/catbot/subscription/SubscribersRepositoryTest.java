package ru.romangr.catbot.subscription;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.romangr.catbot.telegram.model.Chat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roman 11.05.2017.
 */
class SubscribersRepositoryTest {

    private static final Path SUBSCRIBERS_TEST_FILE = Paths.get("src/test/resources/subscribers.json");
    private SubscribersRepository repository = new SubscribersRepository(SUBSCRIBERS_TEST_FILE);

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(SUBSCRIBERS_TEST_FILE);
    }

    @Test
    void getAllSubscribers() {
        repository.addSubscriber(new Chat(1));
        repository.addSubscriber(new Chat(2));
        Collection<Chat> subscribers = repository.getAllSubscribers();
        assertThat(subscribers).hasSize(2);
    }

    @Test
    void addSubscriber() {
        repository.addSubscriber(new Chat(1));
        repository.addSubscriber(new Chat(2));
        SubscribersRepository repositoryAfterRestart = new SubscribersRepository(SUBSCRIBERS_TEST_FILE);
        Collection<Chat> subscribers = repositoryAfterRestart.getAllSubscribers();
        assertThat(subscribers).hasSize(2);
    }

    @Test
    void deleteSubscriber() {
        Chat firstSubscriber = new Chat(1);
        repository.addSubscriber(firstSubscriber);
        repository.addSubscriber(new Chat(2));
        repository.deleteSubscriber(firstSubscriber);
        assertThat(repository.getAllSubscribers()).hasSize(1);
    }

}
