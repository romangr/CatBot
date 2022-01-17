package ru.romangr.catbot.delayed

import org.jooq.ConnectionProvider
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.Semaphore

class SqliteConnectionProvider(private val url: String) : ConnectionProvider {

  private val semaphore: Semaphore = Semaphore(1)
  private var connection: Connection? = null

  override fun acquire(): Connection? {
    semaphore.acquire()
    try {
      this.connection = DriverManager.getConnection(url, "root", "password")
    } catch (e: RuntimeException) {
      log.warn("Error creating db connection", e)
      this.connection = null
      semaphore.release()
    }
    return this.connection
  }

  override fun release(connection: Connection) {
    try {
      connection.close()
    } catch (e: RuntimeException) {
      log.warn("Error closing db connection", e)
    } finally {
      this.connection = null
      semaphore.release()
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(SqliteConnectionProvider::class.java)
  }
}
