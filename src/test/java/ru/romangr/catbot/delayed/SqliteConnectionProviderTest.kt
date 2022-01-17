package ru.romangr.catbot.delayed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

internal class SqliteConnectionProviderTest {

  private lateinit var fileName: String

  @BeforeEach
  internal fun setUp() {
    this.fileName = Files.createTempFile(null, null).toAbsolutePath().toString()
  }

  @AfterEach
  internal fun tearDown() {
    Files.delete(Path.of(fileName))
  }

  @Test
  fun acquireAndRelease() {
    val filePath = Files.createTempFile(null, null).toAbsolutePath().toString()
    val provider = SqliteConnectionProvider("jdbc:sqlite:$filePath")

    val connection = provider.acquire()

    assertThat(connection).isNotNull()
    assertThat(connection?.isValid(1)).isTrue()
    provider.release(connection!!)
    assertThat(connection.isValid(1)).isFalse()
  }
}
