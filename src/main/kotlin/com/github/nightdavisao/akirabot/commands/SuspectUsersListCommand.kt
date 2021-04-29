package com.github.nightdavisao.akirabot.commands

import com.github.nightdavisao.akirabot.AkiraMiscConfig
import com.github.nightdavisao.akirabot.commands.ViewedReportStatsExecutor.Companion.Options.register
import com.github.nightdavisao.akirabot.dao.schemas.ServerJoinedUser
import com.github.nightdavisao.akirabot.tasks.SimiliarNicknameCatcherTask
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import net.perfectdreams.discordinteraktions.commands.SlashCommandArguments
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOptions
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ExecutorService

object SuspectUsersListCommand: SlashCommandDeclaration {
    override fun declaration(): SlashCommandDeclarationBuilder = slashCommand("sususers") {
        description = "Usuários sus"
        subcommand("update") {
            description = "Atualizar lista de sus"
            executor = UpdateSuspectsListExecutor
        }
        subcommand("list") {
            description = "Listar usuários"
            executor = SuspectUsersListExecutor
        }
    }
}

class SuspectUsersListExecutor(val client: Kord, val database: Database, config: AkiraMiscConfig): AkiraCommand(config = config) {
    companion object : SlashCommandExecutorDeclaration(SuspectUsersListExecutor::class)

    override suspend fun executesAkira(context: SlashCommandContext, args: SlashCommandArguments) {
        val serverJoinedUsers = transaction(database) {
            ServerJoinedUser.selectAll().filterNotNull()
        }

        val usersIds = mutableListOf<Long>()
        transaction(database) {
            serverJoinedUsers.forEach {
                val userId = it[ServerJoinedUser.userId]
                usersIds.add(userId)
            }
        }

        val susList = mutableListOf<String>()
        usersIds.forEach {
            val user = client.getUser(Snowflake(it))
            if (user != null)
                susList.add(user.username)
        }

        SimiliarNicknameCatcherTask.getZipDistance(susList).forEach {
            val chunked = buildString {
                append("${it.key} - ${it.value}\n")
            }.chunked(2046)

            chunked.forEach {
                context.sendMessage {
                    content = it
                }
            }
        }
    }
}

class UpdateSuspectsListExecutor(private val executors: ExecutorService, val similiarNicknameCatcherTask: SimiliarNicknameCatcherTask, config: AkiraMiscConfig): AkiraCommand(config = config) {
    companion object : SlashCommandExecutorDeclaration(UpdateSuspectsListExecutor::class)

    override suspend fun executesAkira(context: SlashCommandContext, args: SlashCommandArguments) {
        executors.submit(similiarNicknameCatcherTask)
        context.sendMessage {
            content = "procurando."
        }
    }
}