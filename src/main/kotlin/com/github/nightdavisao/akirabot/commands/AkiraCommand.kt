package com.github.nightdavisao.akirabot.commands

import com.github.nightdavisao.akirabot.AkiraBot
import com.github.nightdavisao.akirabot.AkiraMiscConfig
import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.MessageFlags
import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.commands.SlashCommandArguments
import net.perfectdreams.discordinteraktions.commands.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.context.GuildSlashCommandContext
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOptions

abstract class AkiraCommand(
    val onlyOwner: Boolean = false,
    val onlyStaff: Boolean = false,
    val config: AkiraMiscConfig
) : SlashCommandExecutor() {

    override suspend fun execute(context: SlashCommandContext, args: SlashCommandArguments) {
        context as GuildSlashCommandContext

        if (onlyOwner && context.user.id.value != config.ownerId) {
            context.sendMessage {
                content = "Você não é meu dono :^("
            }
            return
        }

        if (onlyStaff && !context.member.roles.contains(Snowflake(config.supervisorRoleId))) {
            context.sendMessage {
                content = "Não autorizado para usar meus comandos legais"
            }
            return
        }
        executesAkira(context, args)
    }

    abstract suspend fun executesAkira(context: SlashCommandContext, args: SlashCommandArguments)
}