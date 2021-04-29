package com.github.nightdavisao.akirabot.tasks

import com.github.nightdavisao.akirabot.dao.schemas.ServerJoinedUser
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.apache.commons.text.similarity.JaroWinklerDistance
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

class SimiliarNicknameCatcherTask(val client: Kord, val database: Database) : Runnable {
    private val localTimeDate = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))
    private val logger = KotlinLogging.logger { }

    override fun run() = runBlocking {
        val twoWeeksBefore = localTimeDate.with(LocalTime.MIN)
            .minusWeeks(2)
            .toInstant(ZoneOffset.MIN)
            .toEpochMilli()

        val channel = client.getChannelOf<MessageChannel>(Snowflake(303276994202828810L))
        channel?.getMessagesBefore(channel.lastMessageId!!)
            ?.filter { it.author?.id?.value == 297153970613387264L }
            ?.collect { message ->
            val messageTimestamp = message.timestamp.toEpochMilli()

            if (messageTimestamp < twoWeeksBefore) {
                logger.info { "End of joined guild messages" }
                return@collect
            }

            val embed = message.embeds.firstOrNull()

            if (embed != null) {
                val title = embed.title?.removePrefix("\uD83D\uDC4B")
                    ?.trim()
                val userId = embed.footer?.text?.removePrefix("ID do usuário: ")
                    ?.trim()
                    ?.toLong()

                if (title == "Bem-vindo(a)!" && userId != null) {
                    val alreadyExists = transaction(database) {
                        ServerJoinedUser.select {
                            ServerJoinedUser.userId eq userId
                        }.firstOrNull()
                    }

                    if (alreadyExists != null) {
                        logger.info { "Inserting $userId to server joined users" }

                        transaction(database) {
                            ServerJoinedUser.insert {
                                it[this.userId] = userId
                                it[this.timestamp] = messageTimestamp
                            }
                        }
                    }
                }
            }
        }
        return@runBlocking
    }

    companion object {
        private val jaroWinklerDistance = JaroWinklerDistance()

        fun getZipDistance(list: List<String>): MutableMap<String, Int> {
            val mutableMap = mutableMapOf<String, Int>()

            for (element in list) {
                val iterator = list.listIterator()
                while (iterator.hasNext()) {
                    val iter = iterator.next()
                    val applied = jaroWinklerDistance.apply(element, iter)
                    if (applied > 0.93) {
                        println("$element - $iter ($applied)")
                        mutableMap[element] = (mutableMap[element] ?: 0) + 1
                    }
                }
            }
            return mutableMap
        }
    }
}