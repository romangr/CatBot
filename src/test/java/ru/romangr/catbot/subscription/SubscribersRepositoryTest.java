package ru.romangr.catbot.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.romangr.catbot.telegram.model.Chat;

/**
 * Roman 11.05.2017.
 */
class SubscribersRepositoryTest {

  private static final String EXISTING_SUBSCRIBERS_FILE_NAME = "existing-subscribers.json";

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
    Chat subscriber1 = new Chat(1, "title", "first_name", "last_name", "username");
    repository.addSubscriber(subscriber1);
    repository.addSubscriber(new Chat(2, null, null, null, null));
    Collection<Chat> subscribers = repository.getAllSubscribers();
    assertThat(subscribers).hasSize(2);

    Chat firstChat = subscribers.stream()
        .filter(chat -> Long.valueOf(1).equals(chat.getId()))
        .findAny()
        .orElseThrow();
    assertThat(firstChat.getFirstName()).isEqualTo("first_name");
    assertThat(firstChat.getLastName()).isEqualTo("last_name");
    assertThat(firstChat.getTitle()).isEqualTo("title");
    assertThat(firstChat.getUsername()).isEqualTo("username");
  }

  @Test
  void addSubscriber() {
    repository.addSubscriber(new Chat(1, null, null, null, null));
    repository.addSubscriber(new Chat(2, null, null, null, null));
    SubscribersRepository repositoryAfterRestart = new SubscribersRepository(subscribersFile);
    Collection<Chat> subscribers = repositoryAfterRestart.getAllSubscribers();
    assertThat(subscribers).hasSize(2);
  }

  @Test
  void deleteSubscriber() {
    Chat firstSubscriber = new Chat(1, null, null, null, null);
    repository.addSubscriber(firstSubscriber);
    repository.addSubscriber(new Chat(2, null, null, null, null));
    repository.deleteSubscriber(firstSubscriber);
    Collection<Chat> subscribers = repository.getAllSubscribers();
    assertThat(subscribers).hasSize(1);
    assertThat(subscribers.iterator().next().getId()).isEqualTo(2);
  }

  @Test
  void readExistingFile() throws Exception {
    URL subscribersFile
        = this.getClass().getClassLoader().getResource(EXISTING_SUBSCRIBERS_FILE_NAME);
    SubscribersRepository subscribersRepository
        = new SubscribersRepository(Path.of(Objects.requireNonNull(subscribersFile).toURI()));

    assertThat(subscribersRepository.getAllSubscribers())
        .hasSize(1)
        .first()
        .satisfies(chat -> assertThat(chat.getId()).isEqualTo(1))
        .satisfies(chat -> assertThat(chat.getTitle()).isEqualTo("title"))
        .satisfies(chat -> assertThat(chat.getUsername()).isEqualTo("username"))
        .satisfies(chat -> assertThat(chat.getFirstName()).isEqualTo("first_name"))
        .satisfies(chat -> assertThat(chat.getLastName()).isEqualTo("last_name"));
  }

  @Test
  void serializationDeserializationOfAllFields() {
    Chat newSubscriber = new Chat(1, "title", "first", "last", "username");
    repository.addSubscriber(newSubscriber);

    SubscribersRepository newRepository = new SubscribersRepository(subscribersFile);

    assertThat(newRepository.getAllSubscribers())
        .hasSize(1)
        .first()
        .satisfies(chat -> assertThat(chat.getId()).isEqualTo(1))
        .satisfies(chat -> assertThat(chat.getTitle()).isEqualTo("title"))
        .satisfies(chat -> assertThat(chat.getUsername()).isEqualTo("username"))
        .satisfies(chat -> assertThat(chat.getFirstName()).isEqualTo("first"))
        .satisfies(chat -> assertThat(chat.getLastName()).isEqualTo("last"));
  }

  @Test
  void serializationDeserializationWithMissingFields() {
    Chat newSubscriber = new Chat(1, null, "first", null, "username");
    repository.addSubscriber(newSubscriber);

    SubscribersRepository newRepository = new SubscribersRepository(subscribersFile);

    assertThat(newRepository.getAllSubscribers())
        .hasSize(1)
        .first()
        .satisfies(chat -> assertThat(chat.getId()).isEqualTo(1))
        .satisfies(chat -> assertThat(chat.getTitle()).isNull())
        .satisfies(chat -> assertThat(chat.getUsername()).isEqualTo("username"))
        .satisfies(chat -> assertThat(chat.getFirstName()).isEqualTo("first"))
        .satisfies(chat -> assertThat(chat.getLastName()).isNull());
  }
}
