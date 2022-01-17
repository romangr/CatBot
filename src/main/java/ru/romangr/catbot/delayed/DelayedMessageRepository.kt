package ru.romangr.catbot.delayed

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.tables.DelayedPost.DELAYED_POST
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

class DelayedMessageRepository(private val jooqContext: DSLContext) {

  init {
    if (jooqContext.meta().getTables(DELAYED_POST.name).isEmpty()) {
      log.info("No ${DELAYED_POST.name} table found, creating")
      jooqContext.createTable(DELAYED_POST)
          .column(DELAYED_POST.ID)
          .column(DELAYED_POST.TEXT)
          .column(DELAYED_POST.SUBMITTED)
          .constraints(
              DSL.constraint("PK_DELAYED_POST").primaryKey(DELAYED_POST.ID)
          )
          .execute()
    }
  }

  fun count(): Int = jooqContext.selectCount().from(DELAYED_POST).fetch()[0].value1()

  fun nextMessage(): MessageToSubscribers? = jooqContext.select().from(DELAYED_POST)
      .orderBy(DELAYED_POST.SUBMITTED)
      .limit(1)
      .fetch()
      .let {
        if (it.isEmpty()) {
          return null
        }
        val record = it[0]
        jooqContext.delete(DELAYED_POST).where(DELAYED_POST.ID.eq(record[DELAYED_POST.ID])).execute()
        return MessageToSubscribers.textMessage(record[DELAYED_POST.TEXT])
      }

  fun add(message: MessageToSubscribers) {
    jooqContext.insertInto(DELAYED_POST)
        .columns(DELAYED_POST.ID, DELAYED_POST.TEXT, DELAYED_POST.SUBMITTED)
        .values(UUID.randomUUID().toString(), message.text, LocalDateTime.now(Clock.systemUTC()))
        .execute()
  }

  companion object {
    private val log = LoggerFactory.getLogger(DelayedMessageRepository::class.java)
  }
}
