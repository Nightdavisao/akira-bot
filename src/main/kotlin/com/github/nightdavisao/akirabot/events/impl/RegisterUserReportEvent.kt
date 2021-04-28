package com.github.nightdavisao.akirabot.events.impl

import com.github.nightdavisao.akirabot.AkiraMiscConfig
import com.github.nightdavisao.akirabot.events.DiscordEvent
import com.github.nightdavisao.akirabot.dao.schemas.UserReport
import com.github.nightdavisao.akirabot.utils.emote.Emotes
import dev.kord.core.event.message.ReactionAddEvent
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class RegisterUserReportEvent(private val emotes: Emotes,
                              private val database: Database,
                              private val config: AkiraMiscConfig): DiscordEvent<ReactionAddEvent> {
    companion object {
        val logger = KotlinLogging.logger {  }
    }

    override suspend fun executes(event: ReactionAddEvent) {
        if (event.channelId.value == config.reportChannelId) {
            if (event.user.asUser().isBot) return

            val embed = event.message.asMessage().embeds
                .firstOrNull()

            if (embed != null) {
                val reportReason = embed.title?.removePrefix("\uD83D\uDE93")
                    ?.trim()
                val reportViewedBy = event.userId.value
                val timestamp = event.message.asMessage().timestamp.toEpochMilli()

                logger.info { "Checking if report already exists in database... (it may not exist)" }
                val alreadyExists = transaction(database) {
                    UserReport.select {
                        UserReport.messageId eq event.messageId.value
                    }.firstOrNull()
                }

                if (reportReason != null && alreadyExists == null) {
                    logger.info { "Inserting report into database..." }
                    transaction(database) {
                        UserReport.insert {
                            it[this.messageId] = event.messageId.value
                            it[this.reason] = reportReason
                            it[this.reportedAt] = timestamp
                            it[this.viewedBy] = reportViewedBy
                            it[this.approved] = event.emoji.mention == emotes.approved
                            it[this.denied] = event.emoji.mention == emotes.denied
                        }
                    }
                }
            }
        }
    }
}