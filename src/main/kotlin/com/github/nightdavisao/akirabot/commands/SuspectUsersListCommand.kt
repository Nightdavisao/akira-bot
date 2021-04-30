package com.github.nightdavisao.akirabot.commands

import com.github.nightdavisao.akirabot.AkiraMiscConfig
import com.github.nightdavisao.akirabot.tasks.SimiliarAvatarUsersCatcherTask
import net.perfectdreams.discordinteraktions.commands.SlashCommandArguments
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import java.util.concurrent.ExecutorService

object SuspectUsersListCommand: SlashCommandDeclaration {
    override fun declaration(): SlashCommandDeclarationBuilder = slashCommand("sususers") {
        description = "Usuários sus"
        subcommand("update") {
            description = "Atualizar lista de sus"
            executor = UpdateSuspectsListExecutor
        }
    }
}

class UpdateSuspectsListExecutor(private val executors: ExecutorService,
                                 private val similiarNicknameCatcherTask: SimiliarAvatarUsersCatcherTask,
                                 config: AkiraMiscConfig): AkiraCommand(onlyOwner = true, config = config) {
    companion object : SlashCommandExecutorDeclaration(UpdateSuspectsListExecutor::class)

    override suspend fun executesAkira(context: SlashCommandContext, args: SlashCommandArguments) {
        executors.submit(similiarNicknameCatcherTask)
        context.sendMessage {
            content = "Gerando relatório"
        }
    }
}

