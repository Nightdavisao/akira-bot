package com.github.nightdavisao.akirabot.tasks

import com.github.nightdavisao.akirabot.dao.schemas.ServerJoinedUser
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Runnable
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

class SimiliarAvatarUsersCatcherTask(private val client: Kord, private val database: Database) : Runnable {
    private val localTimeDate = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))
    private val logger = KotlinLogging.logger { }

    override fun run() = runBlocking {
        val scopeJob = SupervisorJob()
        val scope = CoroutineScope(scopeJob)
        scope.async {
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

                            if (alreadyExists == null) {
                                logger.info { "Inserting $userId to server joined users" }

                                transaction(database) {
                                    ServerJoinedUser.insert {
                                        it[this.userId] = userId
                                        it[this.timestamp] = messageTimestamp
                                    }
                                }
                            } else {
                                logger.info { "No more need to collect" }
                                this.cancel()
                            }
                        }
                    }
                }
        }.await()

        startSendingMessages()
        return@runBlocking
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun startSendingMessages() = withContext(Dispatchers.IO) {
        val testChannel = client.getChannelOf<MessageChannel>(Snowflake(837744246886629436L))
            ?.asChannelOrNull()

        val usersIds = transaction(database) {
            ServerJoinedUser.selectAll()
                .filterNotNull()
                .map { it[ServerJoinedUser.userId] }
        }

        val userList = buildList {
            usersIds.forEach {
                val user = client.getUser(Snowflake(it))
                if (user != null && user.avatar.data.avatar != null)
                    this.add(
                        UserHolder(
                            user.id.value,
                            user.tag,
                            user.avatar.data.avatar!!
                        )
                    )
            }
        }

        val matchingUsers = getMatchingUsersWithAvatarHashes(userList)

        matchingUsers.forEach { (key, users) ->
            val text = buildString {
                this.append("$key -> ${users.joinToString(", ") { it.id.toString() }}\n")
                this.append(users.joinToString(", ") { it.tag } + "\n")
            }
            testChannel?.createMessage {
                content = "Log $key"
                addFile("log.txt", text.byteInputStream())
            }
        }
    }


    companion object {
        fun getMatchingUsersWithAvatarHashes(list: List<UserHolder>): Map<String, MutableList<UserHolder>> {
            val mutableMap = mutableMapOf<String, MutableList<UserHolder>>()

            for (element in list) {
                val iterator = list.listIterator()
                while (iterator.hasNext()) {
                    val iter = iterator.next()
                    if (element.avatarHash == iter.avatarHash) {
                        val listFromMap = mutableMap[element.avatarHash]
                        if (listFromMap == null) {
                            mutableMap[element.avatarHash] = mutableListOf()
                        }
                        mutableMap[element.avatarHash]?.add(iter)
                    }
                }
            }
            return mutableMap
        }
    }
}

data class UserHolder(
    val id: Long,
    val tag: String,
    val avatarHash: String
)