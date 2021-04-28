package com.github.nightdavisao.akirabot.dao.schemas

import org.jetbrains.exposed.dao.id.IntIdTable

object UserReport: IntIdTable() {
    val messageId = long("messageId")
    val reason = text("reason")
    val reportedAt = long("reportedAt")
    val viewedBy = long("viewedBy").nullable()
    val approved = bool("approved").nullable()
    val denied = bool("denied").nullable()
}