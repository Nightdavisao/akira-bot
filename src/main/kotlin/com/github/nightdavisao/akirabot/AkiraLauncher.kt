package com.github.nightdavisao.akirabot

import com.moandjiezana.toml.Toml
import dev.kord.common.annotation.KordPreview
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

object AkiraLauncher {
    private val toml = Toml()
    private val logger = KotlinLogging.logger {  }

    @KordPreview
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val config = try {
            val file = File("config.toml")
            toml.read(file.readText()).to(AkiraConfig::class.java)
        } catch(e: FileNotFoundException) {
            logger.info { "File config.toml is not found." }
            logger.info { "Configuration files will be created in current directory." }

            copyResourceToFile("/config.toml", "./config.toml")
            copyResourceToFile("/emotes.json", "./emotes.json")
            exitProcess(-1)
        }

        val akiraBot = AkiraBot(config)
        akiraBot.start()
    }

    private fun copyResourceToFile(resourcePath: String, filePath: String) {
        val resource = AkiraLauncher.javaClass.getResourceAsStream(resourcePath)

        if (resource != null) {
            File(filePath).writeBytes(resource.readAllBytes())
            resource.close()
        }
    }
}