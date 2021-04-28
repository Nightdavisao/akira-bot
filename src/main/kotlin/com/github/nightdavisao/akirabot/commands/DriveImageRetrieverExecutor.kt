package com.github.nightdavisao.akirabot.commands

import com.github.nightdavisao.akirabot.AkiraBot
import com.github.nightdavisao.akirabot.AkiraMiscConfig
import com.github.nightdavisao.akirabot.commands.DriveImageRetrieverExecutor.Companion.Options.imageLink
import com.github.nightdavisao.akirabot.utils.AkiraUtils
import io.ktor.client.*
import io.ktor.client.request.*
import net.perfectdreams.discordinteraktions.commands.SlashCommandArguments
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOptions


class DriveImageRetrieverExecutor(val ktor: HttpClient, config: AkiraMiscConfig) : AkiraCommand(
    onlyStaff = true, config = config) {
    companion object : SlashCommandExecutorDeclaration(DriveImageRetrieverExecutor::class) {
        object Options : CommandOptions() {
            val imageLink = string("drivelink", "Um link de uma imagem no GDrive")
                .register()
        }
        override val options: CommandOptions = Options
    }

    override suspend fun executesAkira(context: SlashCommandContext, args: SlashCommandArguments) {
        val url = args[imageLink]

        if (url.startsWith("https://drive.google.com/file/d/")) {
            context.defer()

            val image = AkiraUtils.retrieveImageFromDrive(url, ktor)
            val extension = when (image?.mimeType) {
                "image/png" -> "png"
                "image/jpeg" -> "jpg"
                "image/bmp" -> "bmp"
                else -> "png"
            }

            if (image != null) {
                val downloadedImage = ktor.get<ByteArray>(image.url)

                context.sendMessage {
                    content = "(─‿‿─)"
                    addFile("unknown.$extension", downloadedImage.inputStream())
                }
            } else {
                context.sendEphemeralMessage {
                    content = "Não foi possível baixar a imagem..."
                }
            }
        } else {
            context.sendEphemeralMessage {
                content = "Link inválido da imagem do Drive"
            }
        }
    }
}