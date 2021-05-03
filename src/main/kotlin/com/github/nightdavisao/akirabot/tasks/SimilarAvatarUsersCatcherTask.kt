package com.github.nightdavisao.akirabot.tasks

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.nio.charset.Charset
import java.time.format.DateTimeFormatter

class SimilarAvatarUsersCatcherTask(
    private val client: Kord,
    private val formatter: DateTimeFormatter
) : Runnable {
    private val logger = KotlinLogging.logger { }

    @OptIn(KordPreview::class)
    override fun run() = runBlocking(Dispatchers.IO) {
        val guild = client.getGuild(Snowflake(297732013006389252L))
        val testChannel = client.getChannelOf<MessageChannel>(Snowflake(837744246886629436L))
            ?.asChannelOrNull()
        val membersList = mutableListOf<Member>()

        guild?.withStrategy(EntitySupplyStrategy.rest)
            ?.members
            ?.collect {
                logger.info { "Collecting $it" }
                membersList.add(it)
            }
        val textLog = buildString {
            membersList.asSequence()
                .sortedByDescending { it.id.timeStamp.epochSecond }
                .groupBy { it.data.avatar }
                .filter { it.value.size > 1 }
                .toList()
                .sortedByDescending { it.second.size }
                .toList().toMap()
                .forEach { (avatarHash, members) ->
                    this.append("[${avatarHash ?: "sem avatar"}] ${members.size} membros com mesmo hash de avatar\n")
                    this.append("ID do usuário — (tag do usuário, data de criação da conta)\n")
                    members.forEach {
                        this.append("${it.id.value} — (${it.tag}, ${formatter.format(it.id.timeStamp)})\n")
                    }
                }
        }
        testChannel?.createMessage {
            content = "${System.currentTimeMillis()}"
            addFile("log.txt", textLog.byteInputStream(Charset.defaultCharset()))
        }
        return@runBlocking
    }
}