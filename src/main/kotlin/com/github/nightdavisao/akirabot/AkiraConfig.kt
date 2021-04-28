package com.github.nightdavisao.akirabot

data class AkiraConfig(
    val applicationId: Long,
    val token: String,
    val slashConfig: AkiraSlashCommandsConfig,
    val miscConfig: AkiraMiscConfig
)

data class AkiraSlashCommandsConfig(
    val connectionType: AkiraConnection,
    val publicKey: String,
    // Guilds the bot should update its commands
    val guildIds: List<Long>,
    val serverPort: Int?
)

data class AkiraMiscConfig(
    val ownerId: Long,
    // ATENÇÃO GUARDA-COSTAS
    val supervisorRoleId: Long,
    val emotesPath: String,
    val helperId: Long,
    val reportChannelId: Long
)