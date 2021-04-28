package com.github.nightdavisao.akirabot.events.impl

import com.github.nightdavisao.akirabot.AkiraMiscConfig
import com.github.nightdavisao.akirabot.events.DiscordEvent
import com.github.nightdavisao.akirabot.utils.AkiraUtils
import com.github.nightdavisao.akirabot.utils.emote.Emotes
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.event.message.ReactionAddEvent
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging

class AkiraReactionEvent(private val client: Kord,
                         private val ktor: HttpClient,
                         private val emotes: Emotes,
                         private val config: AkiraMiscConfig) : DiscordEvent<ReactionAddEvent> {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    override suspend fun executes(event: ReactionAddEvent) {
        val channel = event.message.channel

        if (channel.id.value == config.reportChannelId) {
            if (event.user.id == client.selfId) return

            logger.info { "Valid channel: ${channel.id.asString}" }

            val embed = event.message.asMessage().embeds.firstOrNull()
                ?.footer
                ?.text
                ?.removePrefix("Meta: ")
                ?.let { Snowflake(it) }?.let {
                    channel.getMessageOrNull(
                        it
                    )
                        ?.embeds?.firstOrNull()
                }
            if (embed != null) {
                if (event.emoji.mention == emotes.catEmoji) {
                    val value = embed.fields.find { it.name.trim() == "Imagens" }
                        ?.value

                    value?.split("\n")?.asFlow()
                        ?.collect {
                            val image = AkiraUtils.retrieveImageFromDrive(it, ktor)

                            if (image != null) {
                                ktor.get<ByteArray>(image.url)
                                    .also {
                                        channel.createMessage {
                                            content = " "
                                            addFile("unknown.png", it.inputStream())
                                        }
                                    }
                            }
                        }

                }

                if (event.emoji.mention == emotes.clipboard) {
                    embed.fields.find { it.name == "ID do Usuário" }
                        ?.let {
                            channel.createMessage(it.value)
                        }
                }
            }
        }
    }
}
