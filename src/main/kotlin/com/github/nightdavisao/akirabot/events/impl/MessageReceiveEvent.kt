package com.github.nightdavisao.akirabot.events.impl

import com.github.nightdavisao.akirabot.events.DiscordEvent
import com.github.nightdavisao.akirabot.utils.emote.Emotes
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.MessageCreateEvent
import java.time.format.DateTimeFormatter

class MessageReceiveEvent(private val client: Kord,
                          private val formatter: DateTimeFormatter,
                          private val emotes: Emotes)
    : DiscordEvent<MessageCreateEvent> {

    override suspend fun executes(event: MessageCreateEvent) {
        val channel = event.message.channel
        val guild = event.getGuild()

        if (channel.id.asString == "790308357713559582" && event.message.author?.id?.asString == "761931447207460864") {
            val embed = event.message.embeds.firstOrNull()

            if (embed != null) {

                val reportedID = embed.fields.find { it.name.trim() == "ID do Usuário" }
                    ?.value
                    ?.toLong()

                if (reportedID != null) {
                    val user = client.getUser(Snowflake(reportedID))
                    if (user != null) {
                        val member = guild!!.getMemberOrNull(user.id)
                        val metadata = event.message.id.asString

                        val message = channel.createMessage {
                            content = " "
                            embed {
                                description = buildString {
                                    append("**Usuário denunciado**: `${user.tag}` (${user.id.asString})\n")
                                    append("**Snowflake (data)**: ${formatter.format(user.id.timeStamp)}")

                                    if (member != null) {
                                        append("\nEsse usuário está no Apartamento.")
                                    } else {
                                        append("\nEsse usuário não está no Apartamento!")
                                    }
                                }
                                color = Color(255, 192, 203)
                                thumbnail {
                                    url = user.avatar.url
                                }
                                footer {
                                    text = "Meta: $metadata"
                                }
                            }
                        }

                        message.apply {
                            addReaction(ReactionEmoji.Unicode(emotes.catEmoji))
                            addReaction(ReactionEmoji.Unicode(emotes.clipboard))
                        }
                    } else {
                        channel.createMessage {
                            content = "O ID do usuário denunciado é inválido."
                        }
                    }
                }
            }
        }
    }
}