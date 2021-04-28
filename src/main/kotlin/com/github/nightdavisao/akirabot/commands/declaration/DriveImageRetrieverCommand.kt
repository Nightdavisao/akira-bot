package com.github.nightdavisao.akirabot.commands.declaration

import com.github.nightdavisao.akirabot.commands.DriveImageRetrieverExecutor
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand

object DriveImageRetrieverCommand: SlashCommandDeclaration {
    override fun declaration(): SlashCommandDeclarationBuilder = slashCommand("retrievedriveimg") {
        description = "Extrai uma imagem do Google Drive e envia para o Discord"
        executor = DriveImageRetrieverExecutor
    }
}