package ru.romangr.catbot.delayed

import org.jooq.DSLContext
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.tables.DelayedPost.DELAYED_POST
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

class DelayedMessageRepository(private val jooqContext: DSLContext) {

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
        .values(UUID.randomUUID().toString(), message.text, LocalDate.now(Clock.systemUTC()))
        .execute()
  }


}
