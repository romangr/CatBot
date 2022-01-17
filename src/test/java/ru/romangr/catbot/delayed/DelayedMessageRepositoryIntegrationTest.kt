package ru.romangr.catbot.delayed

import org.assertj.core.api.Assertions.assertThat
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.romangr.catbot.subscription.MessageToSubscribers
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager


internal class DelayedMessageRepositoryIntegrationTest {

  private lateinit var connection: Connection
  private lateinit var repository: DelayedMessageRepository
  private lateinit var fileName: String

  @BeforeEach
  internal fun setUp() {
    this.fileName = Files.createTempFile(null, null).toAbsolutePath().toString()
    this.connection = DriverManager.getConnection("jdbc:sqlite:${fileName}", "root", "password")
    val jooqContext = DSL.using(connection, SQLDialect.SQLITE)
    this.repository = DelayedMessageRepository(jooqContext)
  }

  @AfterEach
  internal fun tearDown() {
    connection.close()
    Files.delete(Path.of(fileName))
  }

  @Test
  internal fun count() {
    assertThat(repository.count()).isEqualTo(0)

    repository.add(MessageToSubscribers.textMessage("text"))

    assertThat(repository.count()).isEqualTo(1)
  }

  @Test
  internal fun nextMessageIsFifoBased() {
    repository.add(MessageToSubscribers.textMessage("text1"))
    repository.add(MessageToSubscribers.textMessage("text2"))
    repository.add(MessageToSubscribers.textMessage("text3"))

    assertThat(repository.nextMessage()?.text).isEqualTo("text1")
    assertThat(repository.nextMessage()?.text).isEqualTo("text2")
    assertThat(repository.nextMessage()?.text).isEqualTo("text3")
  }

  @Test
  internal fun nextMessageReturnsNullIfThereAreNoPosts() {
    assertThat(repository.nextMessage()).isNull()
  }
}
