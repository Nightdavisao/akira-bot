package com.github.nightdavisao.akirabot.commands.declaration

import com.github.nightdavisao.akirabot.commands.SnowflakeTimestampExecutor
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand

object SnowflakeTimestampCommand: SlashCommandDeclaration {
    override fun declaration(): SlashCommandDeclarationBuilder = slashCommand("snowflake") {
        description = "Mostra o timestamp de um Snowflake (ID do Discord)"
        executor = SnowflakeTimestampExecutor
    }
}