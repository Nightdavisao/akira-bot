package com.github.nightdavisao.akirabot.commands.declaration

import com.github.nightdavisao.akirabot.commands.ViewedReportStatsExecutor
import com.github.nightdavisao.akirabot.commands.UpdateReportStatsExecutor
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand

object ReportStatsCommand: SlashCommandDeclaration {
    override fun declaration(): SlashCommandDeclarationBuilder = slashCommand("reportstats") {
        description = "Comando incrível e interessante para a administração"
        subcommand("viewedby") {
            description = "Mostra números incríveis"
            executor = ViewedReportStatsExecutor
        }
        subcommand("update") {
            description = "Atualiza os números incríveis (só o criador do gato pode usar)"
            executor = UpdateReportStatsExecutor
        }
    }
}