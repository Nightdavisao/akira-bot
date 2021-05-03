package com.github.nightdavisao.akirabot.events.impl

import com.github.nightdavisao.akirabot.AkiraMiscConfig
import com.github.nightdavisao.akirabot.events.DiscordEvent
import com.github.nightdavisao.akirabot.utils.emote.Emotes
import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.MessageCreateEvent
import java.time.format.DateTimeFormatter

class MessageReceiveEvent(private val client: Kord,
                          private val formatter: DateTimeFormatter,
                          private val emotes: Emotes,
                          private val config: AkiraMiscConfig)
    : DiscordEvent<MessageCreateEvent> {

    override suspend fun executes(event: MessageCreateEvent) {
        val channel = event.message.channel
        // TODO: move to config file
        val communityGuild = client.getGuild(Snowflake(297732013006389252L))
        val supportGuild = client.getGuild(Snowflake(420626099257475072L))

        if (channel.id.value == config.reportChannelId && event.message.author?.id?.value == config.helperId) {
            val embed = event.message.embeds.firstOrNull()

            if (embed != null) {

                val reportedID = embed.fields.find { it.name.trim() == "ID do Usuário" }
                    ?.value
                    ?.toLong()

                if (reportedID != null) {
                    val user = client.getUser(Snowflake(reportedID))
                    if (user != null) {
                        val communityMember = communityGuild!!.getMemberOrNull(user.id)
                        val supportMember = supportGuild!!.getMemberOrNull(user.id)
                        val metadata = event.message.id.asString

                        val message = channel.createMessage {
                            content = " "
                            embed {
                                description = buildString {
                                    append("**Usuário denunciado**: `${user.tag}` (${user.id.asString})\n")
                                    append("**Snowflake (data)**: ${formatter.format(user.id.timeStamp)}\n")

                                    if (communityMember != null && supportMember != null) {
                                        append("Esse usuário está no Apartamento e no servidor de suporte.\n")
                                    } else if (communityMember != null && supportMember == null) {
                                        append("Esse usuário está no servidor de suporte.\n")
                                    } else if (supportMember != null && communityMember == null) {
                                        append("Esse usuário está no Apartamento.\n")
                                    }

                                    if (communityMember != null) {
                                        if (communityGuild.getMember(client.selfId)
                                            .getPermissions()
                                            .contains(Permission.BanMembers)) {
                                            communityGuild.getBanOrNull(communityMember.id)?.let {
                                                val author = it.user.asUserOrNull()

                                                append("\nBanido do Aparatamento pelo o motivo: ")
                                                append("`${it.reason}`")
                                                if (author != null)
                                                    append("— `${author.tag}`")
                                            }
                                        }
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