package com.github.nightdavisao.akirabot

import com.github.nightdavisao.akirabot.commands.*
import com.github.nightdavisao.akirabot.commands.declaration.*
import com.github.nightdavisao.akirabot.dao.schemas.ServerJoinedUser
import com.github.nightdavisao.akirabot.events.impl.AkiraReactionEvent
import com.github.nightdavisao.akirabot.events.impl.MessageReceiveEvent
import com.github.nightdavisao.akirabot.events.impl.RegisterUserReportEvent
import com.github.nightdavisao.akirabot.dao.schemas.UserReport
import com.github.nightdavisao.akirabot.tasks.SimiliarAvatarUsersCatcherTask
import com.github.nightdavisao.akirabot.tasks.UserReportCatcherTask
import com.github.nightdavisao.akirabot.utils.emote.EmoteManagerImpl
import com.github.nightdavisao.akirabot.utils.emote.Emotes
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.ban
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import net.perfectdreams.discordinteraktions.InteractionsServer
import net.perfectdreams.discordinteraktions.commands.CommandManager
import net.perfectdreams.discordinteraktions.kord.installDiscordInteraKTions
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.concurrent.Executors

class AkiraBot(private val config: AkiraConfig) {
    val ktor = HttpClient(CIO)
    private val database = Database.connect("jdbc:h2:./akira", "org.h2.Driver")
    private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
        .withLocale(Locale("pt", "BR"))
        .withZone(ZoneId.of("America/Sao_Paulo"))
    private val emotes = Emotes(
        EmoteManagerImpl(File(config.miscConfig.emotesPath).readText())
    )

    @OptIn(PrivilegedIntent::class, dev.kord.common.annotation.KordPreview::class)
    suspend fun start() {
        // Criar tabela se não existir
        transaction(database) {
            SchemaUtils.create(
                UserReport,
                ServerJoinedUser
            )
        }
        val fixedExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)

        val applicationId = Snowflake(config.applicationId)
        val client = Kord(config.token) {
            intents = Intents {
                + Intents.all
            }
        }
            .apply {
            AkiraReactionEvent(this, ktor, emotes, config.miscConfig).also {
                this.on(consumer = it::executes)
            }
            MessageReceiveEvent(this, formatter, emotes, config.miscConfig).also {
                this.on(consumer = it::executes)
            }
            RegisterUserReportEvent(emotes, database, config.miscConfig).also {
                this.on(consumer = it::executes)
            }
        }
        client.on<MessageCreateEvent> {
            val message = this.message
            val guild = this.getGuild()

            if (message.author?.id?.value == config.miscConfig.ownerId) {
                if (message.content.trim().startsWith("!!banusers")) {
                    val attachUrl = message.content.removePrefix("!!banusers")
                        .trim()
                    val text = ktor.get<String>(attachUrl)
                    val userIds = text.split("\n").first()
                        .substringAfter("->")
                        .trim()
                        .split(", ")
                        .map { it.toLong() }

                    for (id in userIds) {
                        val snowflake = Snowflake(id)
                        val member = guild?.getMemberOrNull(snowflake)
                        if (member != null) {
                            guild.ban(snowflake) {
                                reason = "Self-bot"
                            }
                        }
                    }
                    message.channel.createMessage("Terminei meu trabalho.")
                }
            }
        }


        val userReportCatcherTask = UserReportCatcherTask(client, database, emotes)
        val similiarNicknameCatcherTask = SimiliarAvatarUsersCatcherTask(client, formatter)
        //fixedExecutor.execute(userReportCatcherTask)

        // De qualquer forma, o bot ainda vai iniciar uma conexão ao gateway do Discord
        // ...já que o bot ouve eventos no gateway
        var interactions: InteractionsServer? = null

        val commandManager = if (config.slashConfig.connectionType == AkiraConnection.GATEWAY) {
            CommandManager(
                client.rest,
                applicationId
            )
        } else if (config.slashConfig.connectionType == AkiraConnection.WEBSERVER
            && config.slashConfig.serverPort != null
        ) {

            interactions = InteractionsServer(
                applicationId = config.applicationId,
                publicKey = config.slashConfig.publicKey,
                token = config.token,
                port = config.slashConfig.serverPort
            )

            interactions.commandManager
        } else {
            throw RuntimeException("Server port is null, fix your config :3")
        }

        commandManager.apply {
            register(PingCommand, PingExecutor(config.miscConfig))
            register(DriveImageRetrieverCommand, DriveImageRetrieverExecutor(ktor, config.miscConfig))
            register(RandomCatCommand, RandomCatExecutor(ktor, config.miscConfig))
            register(SnowflakeTimestampCommand, SnowflakeTimestampExecutor(formatter, config.miscConfig))
            register(
                ReportStatsCommand, ViewedReportStatsExecutor(client, database, emotes, config.miscConfig),
                UpdateReportStatsExecutor(fixedExecutor, userReportCatcherTask, config.miscConfig)
            )
            register(
                SuspectUsersListCommand,
                UpdateSuspectsListExecutor(fixedExecutor, similiarNicknameCatcherTask, config.miscConfig)
            )
        }


        config.slashConfig.guildIds.forEach {
            commandManager.updateAllCommandsInGuild(
                Snowflake(it),
                deleteUnknownCommands = true
            )
        }

        interactions?.start()

        if (config.slashConfig.connectionType == AkiraConnection.GATEWAY) {
            client.gateway.gateways.forEach {
                it.value.installDiscordInteraKTions(
                    commandManager
                )
            }
        }

        client.login {
            status = PresenceStatus.DoNotDisturb
            playing("cock is kil")
        }
    }
}