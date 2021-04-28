package com.github.nightdavisao.akirabot.commands.declaration

import com.github.nightdavisao.akirabot.commands.RandomCatExecutor
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand

object RandomCatCommand: SlashCommandDeclaration {
    override fun declaration(): SlashCommandDeclarationBuilder = slashCommand("randomcat") {
        description = "Envia uma foto de um gato do random.cat"
        executor = RandomCatExecutor
    }
}