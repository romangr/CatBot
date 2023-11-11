package ru.romangr.catbot.delayed

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import ru.romangr.catbot.Tables.MIGRATIONS
import ru.romangr.catbot.subscription.MessageToSubscribers
import ru.romangr.catbot.tables.DelayedPost.DELAYED_POST
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

private const val VIDEO_PHOTO_FORWARDING_MIGRATION_NAME = "support video and photo forwarding"

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

        if (jooqContext.meta().getTables(MIGRATIONS.name).isEmpty()) {
            log.info("No ${MIGRATIONS.name} table found, creating")
            jooqContext.createTable(MIGRATIONS)
                .column(MIGRATIONS.NAME)
                .constraints(
                    DSL.constraint("PK_MIGRATIONS").primaryKey(MIGRATIONS.NAME)
                )
                .execute()
        }

        val videoPhotoMigration = jooqContext.selectFrom(MIGRATIONS)
            .where(MIGRATIONS.NAME.eq(VIDEO_PHOTO_FORWARDING_MIGRATION_NAME))
            .fetch()
        if (videoPhotoMigration.isEmpty()) {
            jooqContext.transaction { _ ->
                jooqContext.alterTable(DELAYED_POST)
                    .alterColumn(DELAYED_POST.TEXT)
                    .dropNotNull()
                    .execute()
                jooqContext.alterTable(DELAYED_POST)
                    .addColumn(DELAYED_POST.DOCUMENT_ID)
                    .execute()
                jooqContext.alterTable(DELAYED_POST)
                    .addColumn(DELAYED_POST.VIDEO_ID)
                    .execute()
                jooqContext.alterTable(DELAYED_POST)
                    .addColumn(DELAYED_POST.PHOTO_ID)
                    .execute()
                jooqContext.insertInto(MIGRATIONS)
                    .columns(MIGRATIONS.NAME)
                    .values(VIDEO_PHOTO_FORWARDING_MIGRATION_NAME)
                    .execute()
            }
        }
    }

    fun count(): Int = jooqContext.selectCount().from(DELAYED_POST).fetch()[0].value1()

    fun nextMessage(): MessageToSubscribers? = jooqContext.selectFrom(DELAYED_POST)
        .orderBy(DELAYED_POST.SUBMITTED)
        .limit(1)
        .fetch()
        .let {
            if (it.isEmpty()) {
                return null
            }
            val record = it[0]
            jooqContext.delete(DELAYED_POST).where(DELAYED_POST.ID.eq(record[DELAYED_POST.ID])).execute()
            if (record[DELAYED_POST.TEXT] != null) {
                return@let MessageToSubscribers.textMessage(record[DELAYED_POST.TEXT])
            }
            if (record[DELAYED_POST.DOCUMENT_ID] != null) {
                return@let MessageToSubscribers.documentMessage(record[DELAYED_POST.DOCUMENT_ID])
            }
            if (record[DELAYED_POST.VIDEO_ID] != null) {
                return@let MessageToSubscribers.videoMessage(record[DELAYED_POST.VIDEO_ID])
            }
            if (record[DELAYED_POST.PHOTO_ID] != null) {
                return@let MessageToSubscribers.photoMessage(record[DELAYED_POST.PHOTO_ID])
            }
            throw RuntimeException("Unsupported message record $record")
        }

    fun add(message: MessageToSubscribers) {
        jooqContext.insertInto(DELAYED_POST)
            .columns(
                DELAYED_POST.ID,
                DELAYED_POST.TEXT,
                DELAYED_POST.VIDEO_ID,
                DELAYED_POST.DOCUMENT_ID,
                DELAYED_POST.PHOTO_ID,
                DELAYED_POST.SUBMITTED
            )
            .values(
                UUID.randomUUID().toString(),
                message.text,
                message.videoId,
                message.documentId,
                message.photoId,
                LocalDateTime.now(Clock.systemUTC())
            )
            .execute()
    }

    companion object {
        private val log = LoggerFactory.getLogger(DelayedMessageRepository::class.java)
    }
}
