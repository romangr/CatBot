package ru.romangr.catbot.subscription;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.romangr.catbot.telegram.model.Chat;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roman 11.05.2017.
 */
class SubscribersRepositoryTest {

    private Path subscribersFile;
    private SubscribersRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        this.subscribersFile = Files.createTempFile(null, "subscribers.json");
        try (Writer w = Files.newBufferedWriter(this.subscribersFile)) {
            w.write("[]");
        }
        this.repository = new SubscribersRepository(subscribersFile);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(subscribersFile);
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
        SubscribersRepository repositoryAfterRestart = new SubscribersRepository(subscribersFile);
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
