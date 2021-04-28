package com.github.nightdavisao.akirabot.commands

import com.github.nightdavisao.akirabot.AkiraBot
import com.github.nightdavisao.akirabot.AkiraMiscConfig
import com.grack.nanojson.JsonParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.perfectdreams.discordinteraktions.commands.SlashCommandArguments
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration

class RandomCatExecutor(val ktor: HttpClient, config: AkiraMiscConfig) : AkiraCommand(config = config) {
    companion object : SlashCommandExecutorDeclaration(RandomCatExecutor::class)

    override suspend fun executesAkira(context: SlashCommandContext, args: SlashCommandArguments) {
        val request = ktor.get<HttpResponse>("http://aws.random.cat/meow")

        if (request.status == HttpStatusCode.OK) {
            val parsed = JsonParser.`object`().from(request.readText())
            context.sendMessage {
                content = parsed.getString("file") ?: "No cat found..."
            }
        }
    }
}

