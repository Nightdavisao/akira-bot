package com.github.nightdavisao.akirabot.tasks

import com.github.nightdavisao.akirabot.dao.schemas.ServerJoinedUser
import com.github.nightdavisao.akirabot.utils.emote.Emotes
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.live.live
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class SimiliarAvatarUsersCatcherTask(
    private val client: Kord,
    private val formatter: DateTimeFormatter
) : Runnable {
    @OptIn(KordPreview::class)
    override fun run() = runBlocking(Dispatchers.IO) {
        val guild = client.getGuild(Snowflake(297732013006389252L))
        val testChannel = client.getChannelOf<MessageChannel>(Snowflake(837744246886629436L))
            ?.asChannelOrNull()

        guild?.members
            ?.toList()
            ?.sortedByDescending { it.id.timeStamp.epochSecond }
            ?.groupBy { it.data.avatar }
            ?.filter { it.value.size > 1 }
            ?.forEach { (avatarHash, members) ->
                val textLog = buildString {
                    this.append("[$avatarHash] ${members.size} membros com mesmo hash de avatar\n")
                    this.append("ID do usuário - (tag do usuário, data de criação da conta)\n")
                    members.forEach {
                        this.append("${it.id.value} - (${it.tag}, ${formatter.format(it.id.timeStamp)})\n")
                    }
                }

                testChannel?.createMessage {
                    content = "$avatarHash"
                    addFile("log.txt", textLog.byteInputStream())
                }
            }
        return@runBlocking
    }
}