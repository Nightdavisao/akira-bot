package com.github.nightdavisao.akirabot.commands

import com.github.nightdavisao.akirabot.AkiraBot
import com.github.nightdavisao.akirabot.AkiraMiscConfig
import net.perfectdreams.discordinteraktions.commands.SlashCommandArguments
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

class PingExecutor(config: AkiraMiscConfig) : AkiraCommand(onlyStaff = true, config = config) {
    companion object: SlashCommandExecutorDeclaration(PingExecutor::class)
    override suspend fun executesAkira(context: SlashCommandContext, args: SlashCommandArguments) {
        val uptime = ManagementFactory.getRuntimeMXBean().uptime
        val formatted = String.format(
            "%d hours, %d minutes, %d seconds",
            TimeUnit.MILLISECONDS.toHours(uptime),
            TimeUnit.MILLISECONDS.toMinutes(uptime) % 60,
            TimeUnit.MILLISECONDS.toSeconds(uptime) % 60
        )
        context.sendMessage {
            content = buildString {
                append("pong ping ping pong")
                append("\n\n\n")
                append("*JVM uptime:* $formatted")
            }
        }
    }
}