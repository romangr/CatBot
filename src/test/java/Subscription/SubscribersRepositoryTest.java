package Subscription;

import Model.Chat;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

/**
 * Roman 11.05.2017.
 */
public class SubscribersRepositoryTest {

    private static final Path SUBSCRIBERS_TEST_FILE = Paths.get("src\\test\\resources\\subscribers.data");
    private SubscribersRepository repository = new SubscribersRepository(SUBSCRIBERS_TEST_FILE);

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(SUBSCRIBERS_TEST_FILE);
    }

    @Test
    public void getAllSubscribers() throws Exception {
        repository.addSubscriber(new Chat(1));
        repository.addSubscriber(new Chat(2));
        Collection<Chat> subscribers = repository.getAllSubscribers();
        assertThat(subscribers, hasSize(2));
    }

    @Test
    public void addSubscriber() throws Exception {
        repository.addSubscriber(new Chat(1));
        repository.addSubscriber(new Chat(2));
        SubscribersRepository repositoryAfterRestart = new SubscribersRepository(SUBSCRIBERS_TEST_FILE);
        Collection<Chat> subscribers = repositoryAfterRestart.getAllSubscribers();
        assertThat(subscribers, hasSize(2));
    }

    @Test
    public void deleteSubscriber() throws Exception {
        Chat firstSubscriber = new Chat(1);
        repository.addSubscriber(firstSubscriber);
        repository.addSubscriber(new Chat(2));
        repository.deleteSubscriber(firstSubscriber);
        assertThat(repository.getAllSubscribers(), hasSize(1));
    }

}