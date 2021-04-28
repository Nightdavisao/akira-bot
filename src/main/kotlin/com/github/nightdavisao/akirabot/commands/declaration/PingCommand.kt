package com.github.nightdavisao.akirabot.commands.declaration

import com.github.nightdavisao.akirabot.commands.PingExecutor
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand

object PingCommand: SlashCommandDeclaration {
    override fun declaration(): SlashCommandDeclarationBuilder = slashCommand("ping") {
        description = "comando que ping e pong"
        executor = PingExecutor
    }
}