package com.github.nightdavisao.akirabot.tasks

import com.github.nightdavisao.akirabot.dao.schemas.UserReport
import com.github.nightdavisao.akirabot.utils.emote.Emotes
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class UserReportCatcherTask(
    private val client: Kord,
    private val database: Database,
    private val emotes: Emotes
) : Runnable {

    companion object {
        val logger = KotlinLogging.logger { }
    }

    override fun run() = runBlocking {
        logger.info { "Deleting ALL entries in the user reports table" }
        transaction(database) {
            UserReport.deleteAll()
        }

        val channel = client.getChannelOf<MessageChannel>(Snowflake(790308357713559582L))

        if (channel?.lastMessageId != null) {
            channel.getMessagesBefore(channel.lastMessageId!!)
                .filter { it.author?.id?.value == 761931447207460864L && it.embeds.isNotEmpty() }
                .collect { message ->
                    val embed = message.embeds.first()

                    val reportReason = embed.title?.removePrefix("\uD83D\uDE93")
                        ?.trim()

                    val reactions = message.reactions.map { reaction ->
                        Pair(reaction.emoji, message.getReactors(reaction.emoji)
                            .toList()
                            .filterNot { it.isBot }.firstOrNull()
                        )
                    }

                    val approved = reactions.find {
                        it.first.mention == emotes.approved
                    }?.second

                    val denied = reactions.find {
                        it.first.mention == emotes.denied
                    }?.second

                    val user = reactions.find { it.second != null }
                        ?.second?.id?.value

                    val timestamp = message.timestamp.toEpochMilli()


                    if (reportReason != null) {
                        logger.info { "This report doesn't exists, inserting..." }

                        transaction(database) {
                            UserReport.insert {
                                it[this.messageId] = message.id.value
                                it[this.reason] = reportReason
                                it[this.reportedAt] = timestamp
                                it[this.viewedBy] = user
                                it[this.approved] = approved != null
                                it[this.denied] = denied != null
                            }
                        }
                    }

                }
        }
    }
}