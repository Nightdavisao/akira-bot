package com.github.nightdavisao.akirabot.commands

import com.github.nightdavisao.akirabot.AkiraMiscConfig
import com.github.nightdavisao.akirabot.commands.SnowflakeTimestampExecutor.Companion.Options.snowflakeLong
import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.commands.SlashCommandArguments
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOptions
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class SnowflakeTimestampExecutor(private val formatter: DateTimeFormatter, config: AkiraMiscConfig)
    : AkiraCommand(config = config) {
    companion object : SlashCommandExecutorDeclaration(SnowflakeTimestampExecutor::class) {
        object Options : CommandOptions() {
            val snowflakeLong = string("long", "Snowflake")
                .register()
        }
        override val options: CommandOptions = Options
    }
    override suspend fun executesAkira(context: SlashCommandContext, args: SlashCommandArguments) {
        val snowflakeArgument = args[snowflakeLong].toLongOrNull()

        if (snowflakeArgument != null) {
            context.sendMessage {
                content = formatter.format(Snowflake(snowflakeArgument).timeStamp)
            }
        } else {
            context.sendEphemeralMessage {
                content = "Snowflake inválido"
            }
        }
    }
}